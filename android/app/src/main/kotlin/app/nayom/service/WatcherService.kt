package app.nayom.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import app.nayom.MainActivity
import app.nayom.data.Blocked
import app.nayom.data.Detector
import app.nayom.data.Prefs
import app.nayom.data.Vault

/** Sees which app you are in and what you type. Never reads password fields or banking apps. */
class WatcherService : AccessibilityService() {
    private val ui = Handler(Looper.getMainLooper())
    private var pending: Runnable? = null
    private var lastText = ""
    private var pkg = ""

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        val p = e.packageName?.toString() ?: return
        val prefs = Prefs(this)
        when (e.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (p == "com.android.systemui" || p.contains("inputmethod") || p.contains("keyboard")) return
                pkg = p
                OverlayService.appHidden(p == packageName || Blocked.app(p) || p in prefs.hideIn)
            }
            AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                if (!prefs.watchTyping || e.isPassword || Blocked.app(p) || p == packageName) return
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
        store.log("typed-${hit.kind}", "$app | $text")
        val open = Intent(this, MainActivity::class.java).putExtra("what", text.take(80)).putExtra("who", "")
        when (hit.kind) {
            "reflex" -> OverlayService.say("A quick yes? Stop. Do you actually have the time for this?", 9000, open)
            else -> OverlayService.say("That sounds like a promise${hit.time?.let { " ($it)" } ?: ""}. Tap to put it on the clock before you send.", 10000, open)
        }
    }

    override fun onInterrupt() {}
}
