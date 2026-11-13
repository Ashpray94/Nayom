package app.nayom.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import app.nayom.MainActivity
import app.nayom.data.Blocked
import app.nayom.data.Detector
import app.nayom.data.Prefs
import app.nayom.data.Vault

/** Sees which app you are in, what you do (tap, scroll, type) and what you write. Never reads password fields or banking apps. */
class WatcherService : AccessibilityService() {
    private val ui = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null
    private var lastText = ""
    private var lastScrollY = 0
    private var gStart = 0L; private var gLast = 0L; private var gY0 = 0; private var gY = 0; private var gIdx0 = -1; private var gIdx = -1
    private var gDelta = 0; private var gEvents = 0
    private val endGesture = Runnable { endGesture() }
    private fun endGesture() {
        val dm = resources.displayMetrics; val net = maxOf(kotlin.math.abs(gY - gY0), kotlin.math.abs(gDelta)); val dur = maxOf(60L, gLast - gStart)
        val screens = net.toFloat() / dm.heightPixels; val speed = screens * 1000f / dur
        val changed = gIdx0 >= 0 && gIdx != gIdx0
        val dir = if (gY >= gY0) 1 else -1
        val swipe = changed || (screens >= 0.35f && speed >= 0.8f)
        if (swipe) OverlayService.react("scroll", dir) else if (net > dp(40)) OverlayService.react("nod", dir)
        runCatching { Vault.open(this).log("gesture", "${if (swipe) "swipe" else "drag"} ${(screens * 100).toInt()}% in ${dur}ms idx=${gIdx0}>${gIdx} events=$gEvents dY=$gDelta y=${gY0}>${gY} app=${OverlayService.fgPkg}") }
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        val p = e.packageName?.toString() ?: return
        val prefs = Prefs(this)
        when (e.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (p == "com.android.systemui" || p == "android" || p.contains("inputmethod") || p.contains("keyboard") || p.contains("honeyboard") || p.contains("launcher") || p.contains("permissioncontroller")) return
                if (p == packageName && e.className?.toString()?.contains("Activity") != true) return
                OverlayService.fgPkg = p
                if (p != packageName) app.nayom.data.Habits(this).switchTo(p)
                OverlayService.appHidden(p == packageName || p in prefs.hideIn)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> if (!Blocked.app(p)) { runCatching { val b = android.graphics.Rect(); e.source?.getBoundsInScreen(b); if (!b.isEmpty) OverlayService.look(b.centerX(), b.centerY()) } }
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> if (!Blocked.app(p)) {
                // A gesture = scroll events until 400 ms of quiet. It is judged on how it ENDED, not on the wiggles on the way:
                //   SWIPE = a flick that changed the item (page/reel index changed) or travelled >= 35% of the screen at >= 0.8 screens/s
                //   DRAG  = anything slower or shorter, including moving and coming back. Drags never count.
                val now = System.currentTimeMillis(); val y = e.scrollY
                if (now - gLast > 400) { gStart = now; gY0 = y; gIdx0 = e.fromIndex; gDelta = 0; gEvents = 0 }
                if (android.os.Build.VERSION.SDK_INT >= 28) gDelta += e.scrollDeltaY
                gEvents++; gLast = now; gY = y; gIdx = e.fromIndex
                ui.removeCallbacks(endGesture); ui.postDelayed(endGesture, 400)
            }
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> OverlayService.react("notify")
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                if (!prefs.watchTyping || e.isPassword || Blocked.app(p) || p == packageName) return
                OverlayService.react("typing")
                runCatching { val b = android.graphics.Rect(); e.source?.getBoundsInScreen(b); if (!b.isEmpty) OverlayService.typing(b.left, b.top, b.right, b.bottom, e.text.joinToString("").take(e.fromIndex + e.addedCount)) }
                val text = e.text.joinToString("").trim()
                if (text.isEmpty() || text == lastText || Blocked.text(text)) return
                lastText = text
                pending?.let { ui.removeCallbacks(it) }
                pending = Runnable { learn(p, text) }.also { ui.postDelayed(it, 1500) }
            }
        }
    }

    private val fixer by lazy { app.nayom.data.Fixer(this) { OverlayService.fixes(it) } }

    private fun learn(app: String, text: String) {
        if (text.length >= 4) fixer.check(text)
        val store = Vault.open(this)
        store.typed(app.substringAfterLast('.'), text)
        val hit = Detector.promise(text) ?: return
        val who = runCatching { contactOnScreen(text) }.getOrNull().orEmpty()
        store.log("typed-${hit.kind}", "$app | to=$who | $text")
        val dueIdx = when { hit.time == null -> 1; Regex("(?i)tomorrow|tmrw|kal").containsMatchIn(hit.time) -> 2; Regex("(?i)tonight|today|aaj|raat|shaam").containsMatchIn(hit.time) -> 1
            Regex("(?i)\\d+ ?(min|hour|hr)").containsMatchIn(hit.time) -> 0; else -> 1 }
        val open = Intent(this, MainActivity::class.java).putExtra("what", text.take(80)).putExtra("who", who).putExtra("due", dueIdx)
        val to = if (who.isNotBlank()) " to $who" else ""
        when (hit.kind) {
            "reflex" -> OverlayService.say("A quick yes$to? Stop. Do you actually have the time for this?", 0, open)
            else -> OverlayService.say("That sounds like a promise$to${hit.time?.let { " ($it)" } ?: ""}. Tap to put it on the clock before you send.", 0, open)
        }
        OverlayService.react("warn")
    }

    /** Who you are talking to: the conversation title near the top of the screen (chat toolbar), never the text you typed. */
    private fun contactOnScreen(typed: String): String {
        val root = rootInActiveWindow ?: return ""
        val best = mutableListOf<Pair<Int, String>>()
        val r = Rect()
        fun walk(n: AccessibilityNodeInfo?, depth: Int) {
            if (n == null || depth > 25) return
            val t = n.text?.toString()?.trim().orEmpty()
            if (t.isNotEmpty() && t.length in 2..32 && t != typed && !n.isEditable && !n.isPassword) {
                n.getBoundsInScreen(r)
                val id = n.viewIdResourceName.orEmpty().lowercase()
                val named = id.contains("contact_name") || id.contains("conversation_title") || id.contains("toolbar") || id.contains("title") || id.contains("name")
                if (r.top < 420 && r.height() > 0 && !t.contains("online", true) && !t.contains("typing", true) && !t.matches(Regex("[\\d:./ ]+")))
                    best += (if (named) r.top - 1000 else r.top) to t
            }
            for (i in 0 until n.childCount) walk(n.getChild(i), depth + 1)
        }
        walk(root, 0)
        return best.minByOrNull { it.first }?.second.orEmpty()
    }

    override fun onInterrupt() {}
}
