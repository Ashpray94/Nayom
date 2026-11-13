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

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        val p = e.packageName?.toString() ?: return
        val prefs = Prefs(this)
        when (e.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (p == "com.android.systemui" || p.contains("inputmethod") || p.contains("keyboard")) return
                OverlayService.fgPkg = p
                OverlayService.appHidden(p == packageName || Blocked.app(p) || p in prefs.hideIn)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> if (!Blocked.app(p)) OverlayService.react("click")
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> if (!Blocked.app(p)) {
                val y = e.scrollY; OverlayService.react("scroll", if (y >= lastScrollY) 1 else -1); lastScrollY = y
            }
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> OverlayService.react("notify")
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                if (!prefs.watchTyping || e.isPassword || Blocked.app(p) || p == packageName) return
                OverlayService.react("typing")
                val text = e.text.joinToString("").trim()
                if (text.isEmpty() || text == lastText || Blocked.text(text)) return
                lastText = text
                pending?.let { ui.removeCallbacks(it) }
                pending = Runnable { learn(p, text) }.also { ui.postDelayed(it, 1500) }
            }
        }
    }

    private fun learn(app: String, text: String) {
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
