package app.nayom.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.nayom.MainActivity
import app.nayom.R
import app.nayom.data.CalendarBridge
import app.nayom.data.Prefs
import app.nayom.data.Status
import app.nayom.data.Vault
import java.time.Duration
import java.time.ZonedDateTime

/** The floating head. Sits above every app, says short things in a bubble, watches deadlines and calls. */
class OverlayService : Service() {
    companion object {
        @Volatile var instance: OverlayService? = null
        fun say(text: String, ms: Long = 8000, tap: Intent? = null) { instance?.show(text, ms, tap) }
        fun appHidden(h: Boolean) { instance?.setAppHidden(h) }
        fun start(ctx: Context) { ctx.startForegroundService(Intent(ctx, OverlayService::class.java)) }
        fun stop(ctx: Context) { ctx.stopService(Intent(ctx, OverlayService::class.java)) }
    }

    private lateinit var wm: WindowManager
    private lateinit var root: LinearLayout
    private lateinit var bubble: TextView
    private lateinit var head: ImageView
    private lateinit var lp: WindowManager.LayoutParams
    private val ui = Handler(Looper.getMainLooper())
    private val warned = mutableSetOf<String>()
    private var hiddenByApp = false
    private var tapIntent: Intent? = null
    private var lastChatter = System.currentTimeMillis()
    private val chatter = listOf("What did you promise today?", "Water. Now.", "Anyone waiting on a reply from you?", "Phone down for five minutes.",
        "Eat something. Then work.", "Is this the thing you meant to do?", "Check your Promises. I will wait.")

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate(); instance = this
        val ch = "nayom_overlay"
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(ch, "Nayom is watching over you", NotificationManager.IMPORTANCE_MIN))
        val n = Notification.Builder(this, ch).setSmallIcon(android.R.drawable.presence_online).setContentTitle("Nayom is with you")
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)).build()
        if (Build.VERSION.SDK_INT >= 34) startForeground(7, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(7, n)
        build(); watchCalls(); ui.postDelayed(tick, 5000)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun build() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val p = Prefs(this)
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.END }
        bubble = TextView(this).apply {
            setTextColor(0xFF0F2E2C.toInt()); textSize = 14f; typeface = Typeface.DEFAULT_BOLD; maxWidth = dp(240)
            setPadding(dp(14), dp(10), dp(14), dp(10)); visibility = View.GONE
            background = GradientDrawable().apply { setColor(0xFFFFFFFF.toInt()); cornerRadius = dp(18).toFloat(); setStroke(dp(1), 0x330F2E2C) }
            elevation = dp(6).toFloat()
            setOnClickListener { tapIntent?.let { i -> i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP); startActivity(i) }; visibility = View.GONE }
        }
        head = ImageView(this).apply {
            setImageResource(R.drawable.head_avatar); elevation = dp(8).toFloat()
            layoutParams = LinearLayout.LayoutParams(dp(64), dp(64)).apply { topMargin = dp(6) }
        }
        root.addView(bubble); root.addView(head)
        lp = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.END; x = p.avatarX; y = p.avatarY }
        var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false
        head.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; ox = lp.x; oy = lp.y; moved = false }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (sx - e.rawX).toInt(); val dy = (e.rawY - sy).toInt()
                    if (kotlin.math.abs(dx) + kotlin.math.abs(dy) > 12) moved = true
                    if (moved) { lp.x = ox + dx; lp.y = oy + dy; wm.updateViewLayout(root, lp) }
                }
                MotionEvent.ACTION_UP -> {
                    if (moved) { p.avatarX = lp.x; p.avatarY = lp.y }
                    else show(summary(), 7000, Intent(this, MainActivity::class.java))
                }
            }
            true
        }
        wm.addView(root, lp)
    }

    private fun summary(): String {
        val s = Vault.open(this); val open = s.promises().filter { it.status == Status.Open }
        val n = open.firstOrNull() ?: return "Nothing is owed right now. Keep it that way."
        val m = Duration.between(ZonedDateTime.now(), n.due).toMinutes()
        val busy = CalendarBridge(this).busyHours(ZonedDateTime.now())
        val load = if (busy >= 6) " Your day is packed." else ""
        return "${open.size} open. Next: ${n.what} for ${n.person} in ${if (m < 60) "$m min" else "${m / 60} h"}.$load"
    }

    fun show(text: String, ms: Long, tap: Intent?) = ui.post {
        if (hiddenByApp) return@post
        tapIntent = tap; bubble.text = text; bubble.visibility = View.VISIBLE
        ui.removeCallbacks(hideBubble); ui.postDelayed(hideBubble, ms)
    }
    private val hideBubble = Runnable { bubble.visibility = View.GONE }

    fun setAppHidden(h: Boolean) = ui.post {
        hiddenByApp = h
        root.visibility = if (h || !Prefs(this).avatarOn) View.GONE else View.VISIBLE
    }

    // ---- deadlines and the odd word of nagging
    private val tick = object : Runnable { override fun run() {
        runCatching { check() }
        ui.postDelayed(this, 30_000)
    } }
    private fun check() {
        val s = Vault.open(this); s.sweepMissed()
        val now = ZonedDateTime.now()
        s.promises().filter { it.status == Status.Open }.forEach { p ->
            val m = Duration.between(now, p.due).toMinutes()
            for (t in listOf(60, 15, 5)) if (m in 0..t.toLong() && warned.add("${p.id}:$t"))
                show("${p.person}: ${p.what}. $m min left.", 12000, Intent(this, MainActivity::class.java))
        }
        s.promises().lastOrNull { it.status == Status.Missed && it.id !in s.debriefed() }?.let {
            if (warned.add("miss:${it.id}")) show("You missed ${it.person}'s ${it.what.lowercase()}. Come and tell me why.", 15000, Intent(this, MainActivity::class.java))
        }
        if (System.currentTimeMillis() - lastChatter > 25 * 60_000 && bubble.visibility != View.VISIBLE) { lastChatter = System.currentTimeMillis(); show(chatter.random(), 6000, null) }
    }

    // ---- calls: remind before you say yes to anything
    private fun watchCalls() {
        runCatching {
            val tm = getSystemService(TelephonyManager::class.java)
            tm.registerTelephonyCallback(mainExecutor, object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    if (state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) {
                        hiddenByApp = false; root.visibility = View.VISIBLE
                        show("On a call? Do not say yes to anything you cannot do. ${summary()}", 15000, null)
                    }
                }
            })
        }
    }

    override fun onDestroy() { instance = null; ui.removeCallbacksAndMessages(null); runCatching { wm.removeView(root) }; super.onDestroy() }
}
