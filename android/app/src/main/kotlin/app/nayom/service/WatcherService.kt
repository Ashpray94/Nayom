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
    private var lastIndex = -1
    private var accDist = 0
    private var lastMoveAt = 0L
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
                // distance tracker: a swipe counts only after ~40% of the screen height has really moved
                val dm = resources.displayMetrics; val now = System.currentTimeMillis()
                val y = e.scrollY
                var d = maxOf(kotlin.math.abs(if (android.os.Build.VERSION.SDK_INT >= 28) e.scrollDeltaY else 0), kotlin.math.abs(y - lastScrollY))
                if (d == 0 && e.fromIndex != lastIndex) d = dm.heightPixels / 2          // pager/reels: one page flip, no pixel delta
                val dir = if (y >= lastScrollY) 1 else -1
                lastScrollY = y; lastIndex = e.fromIndex
                if (now - lastMoveAt > 1200) accDist = 0                                    // a pause starts a fresh gesture
                lastMoveAt = now; accDist += d
                if (d > dp(40)) OverlayService.react("nod", dir)                            // small glance for any real movement
                if (accDist >= dm.heightPixels * 0.40) { accDist = 0; OverlayService.react("scroll", dir) }
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
