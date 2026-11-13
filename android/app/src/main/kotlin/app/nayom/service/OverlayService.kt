package app.nayom.service

import android.animation.ValueAnimator
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
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
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import app.nayom.MainActivity
import app.nayom.R
import app.nayom.data.CalendarBridge
import app.nayom.data.Prefs
import app.nayom.data.Status
import app.nayom.data.Vault
import java.time.Duration
import java.time.ZonedDateTime

/**
 * The floating head. Sits above every app, reacts to what you do, speaks in a separate wide bubble,
 * watches deadlines and calls. Hiding the head frees the screen; Nayom keeps tracking.
 */
class OverlayService : Service() {
    companion object {
        @Volatile var instance: OverlayService? = null
        @Volatile var fgPkg: String = ""
        fun say(text: String, ms: Long = 0, tap: Intent? = null) { instance?.show(text, ms, tap) }
        fun appHidden(h: Boolean) { instance?.setAppHidden(h) }
        fun react(kind: String, dir: Int = 0) { instance?.reactTo(kind, dir) }
        fun start(ctx: Context) { ctx.startForegroundService(Intent(ctx, OverlayService::class.java)) }
        fun command(ctx: Context, action: String) { ctx.startForegroundService(Intent(ctx, OverlayService::class.java).setAction(action)) }
    }

    private lateinit var wm: WindowManager
    private lateinit var headRoot: LinearLayout
    private lateinit var head: ImageView
    private lateinit var menu: LinearLayout
    private lateinit var headLp: WindowManager.LayoutParams
    private lateinit var bubbleRoot: FrameLayout
    private lateinit var bubbleText: TextView
    private lateinit var bubbleLp: WindowManager.LayoutParams
    private var bubbleUp = false
    private lateinit var prefs: Prefs
    private val ui = Handler(Looper.getMainLooper())
    private val warned = mutableSetOf<String>()
    private var hiddenByApp = false
    private var tapIntent: Intent? = null
    private var lastChatter = System.currentTimeMillis()
    private var lastReact = 0L
    private var lastType = 0L
    private var scrolls = 0; private var scrollWin = 0L; private var lastDoom = 0L
    private var lastExpectHr = -1
    private var wasteLevel = 0; private var wasteApp = ""
    private var savedY = -1
    @Volatile private var lastInput = System.currentTimeMillis()
    private var breathing: ValueAnimator? = null
    private var watching = false
    private val videoApps = listOf("youtube", "netflix", "hotstar", "primevideo", "instagram", "tiktok", "mxplayer", "vlc", "jio", "zee5", "sonyliv", "twitch")
    private val chatter = listOf("What did you promise today?", "Water. Now.", "Anyone waiting on a reply from you?", "Phone down for five minutes.",
        "Eat something. Then work.", "Is this the thing you meant to do?", "Check your Promises. I will wait.")

    override fun onBind(i: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate(); instance = this; prefs = Prefs(this)
        val ch = "nayom_overlay"
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(ch, "Nayom is with you", NotificationManager.IMPORTANCE_MIN))
        val showHide = PendingIntent.getForegroundService(this, 1,
            Intent(this, OverlayService::class.java).setAction(if (prefs.headHidden) "SHOW" else "HIDE"), PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(this, ch).setSmallIcon(android.R.drawable.presence_online).setContentTitle("Nayom is with you")
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .addAction(Notification.Action.Builder(null, if (prefs.headHidden) "Show head" else "Hide head", showHide).build()).build()
        if (Build.VERSION.SDK_INT >= 34) startForeground(7, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE) else startForeground(7, n)
        build(); watchCalls(); applyVisibility(); ui.postDelayed(tick, 5000); ui.postDelayed(mood, 3000)
    }

    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        when (i?.action) { "HIDE" -> { prefs.headHidden = true; applyVisibility(); refreshNotification() }
            "SHOW" -> { prefs.headHidden = false; applyVisibility(); refreshNotification() } }
        return START_STICKY
    }

    private fun refreshNotification() {
        val ch = "nayom_overlay"
        val a = PendingIntent.getForegroundService(this, 1, Intent(this, OverlayService::class.java).setAction(if (prefs.headHidden) "SHOW" else "HIDE"), PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(this, ch).setSmallIcon(android.R.drawable.presence_online).setContentTitle("Nayom is with you")
            .setContentIntent(PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .addAction(Notification.Action.Builder(null, if (prefs.headHidden) "Show head" else "Hide head", a).build()).build()
        getSystemService(NotificationManager::class.java).notify(7, n)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun pill(label: String, fg: Int = 0xFF0F2E2C.toInt(), bg: Int = 0xFFFFFFFF.toInt(), click: () -> Unit) = TextView(this).apply {
        text = label.uppercase(); setTextColor(fg); textSize = 11f; typeface = Typeface.DEFAULT_BOLD; letterSpacing = 0.1f
        setPadding(dp(14), dp(9), dp(14), dp(9)); gravity = Gravity.CENTER
        background = GradientDrawable().apply { setColor(bg); cornerRadius = dp(20).toFloat(); setStroke(dp(1), 0x330F2E2C) }
        elevation = dp(4).toFloat(); setOnClickListener { click() }
        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(6) }
    }

    private fun build() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        // head + tap menu
        head = ImageView(this).apply { setImageResource(R.drawable.head_avatar); elevation = dp(8).toFloat()
            layoutParams = LinearLayout.LayoutParams(dp(64), dp(64)) }
        menu = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.END; visibility = View.GONE }
        menu.addView(pill("Open app") { menu.visibility = View.GONE; startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) })
        menu.addView(pill("Messages") { menu.visibility = View.GONE; startActivity(Intent(this, MainActivity::class.java).putExtra("route", "inbox").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) })
        menu.addView(pill("Hide head", 0xFFFFFFFF.toInt(), 0xFF22706A.toInt()) { menu.visibility = View.GONE; prefs.headHidden = true; applyVisibility(); refreshNotification() })
        headRoot = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.END; addView(head); addView(menu) }
        headLp = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.END; x = prefs.avatarX; y = prefs.avatarY }
        var sx = 0f; var sy = 0f; var ox = 0; var oy = 0; var moved = false
        head.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; ox = headLp.x; oy = headLp.y; moved = false }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (sx - e.rawX).toInt(); val dy = (e.rawY - sy).toInt()
                    if (kotlin.math.abs(dx) + kotlin.math.abs(dy) > 12) moved = true
                    if (moved) { headLp.x = ox + dx; headLp.y = oy + dy; wm.updateViewLayout(headRoot, headLp) }
                }
                MotionEvent.ACTION_UP -> {
                    if (moved) { prefs.avatarX = headLp.x; prefs.avatarY = headLp.y }
                    else { reactTo("click", 0); menu.visibility = if (menu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        if (menu.visibility == View.VISIBLE) { show(summary(), 9000, null); ui.postDelayed({ menu.visibility = View.GONE }, 9000) } }
                }
            }
            true
        }
        wm.addView(headRoot, headLp)

        // bubble: its own wide window, scrollable, up to half the screen height
        val dm = resources.displayMetrics
        bubbleText = TextView(this).apply { setTextColor(0xFF0F2E2C.toInt()); textSize = 17f; setLineSpacing(0f, 1.15f); setPadding(dp(20), dp(18), dp(20), dp(18)) }
        val scroll = ScrollView(this).apply { addView(bubbleText); isVerticalScrollBarEnabled = false }
        bubbleRoot = FrameLayout(this).apply {
            background = GradientDrawable().apply { setColor(0xFFFFFFFF.toInt()); cornerRadius = dp(24).toFloat(); setStroke(dp(1), 0x330F2E2C) }
            elevation = dp(10).toFloat(); visibility = View.GONE; addView(scroll)
            setOnClickListener { tapIntent?.let { i -> startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)) }; dismissBubble() }
        }
        bubbleLp = WindowManager.LayoutParams(dm.widthPixels - dp(32), WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
        wm.addView(bubbleRoot, bubbleLp)
    }

    // ---- show/hide
    private fun applyVisibility() = ui.post {
        val vis = if (hiddenByApp || prefs.headHidden) View.GONE else View.VISIBLE
        headRoot.visibility = vis
        if (vis == View.GONE) { bubbleRoot.visibility = View.GONE; menu.visibility = View.GONE }
    }
    fun setAppHidden(h: Boolean) { if (h != hiddenByApp) { hiddenByApp = h; applyVisibility() } }

    // ---- bubble
    fun show(text: String, ms: Long, tap: Intent?) = ui.post {
        if (hiddenByApp || prefs.headHidden) return@post
        tapIntent = tap; bubbleText.text = text
        val dm = resources.displayMetrics; val maxH = dm.heightPixels / 2
        bubbleText.measure(View.MeasureSpec.makeMeasureSpec(dm.widthPixels - dp(32), View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED)
        val h = minOf(bubbleText.measuredHeight, maxH)
        (bubbleRoot.getChildAt(0) as ScrollView).layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, h)
        val typing = System.currentTimeMillis() - lastType < 10_000
        if (typing) { if (savedY < 0) savedY = headLp.y; headLp.y = dp(36); runCatching { wm.updateViewLayout(headRoot, headLp) } }
        else restoreHead()
        val headY = headLp.y
        bubbleLp.y = if (headY < dm.heightPixels / 2) headY + dp(76) else maxOf(dp(48), headY - h - dp(12))
        wm.updateViewLayout(bubbleRoot, bubbleLp)
        bubbleRoot.visibility = View.VISIBLE; bubbleRoot.alpha = 0f; bubbleRoot.translationY = if (headY < dm.heightPixels / 2) -dp(16).toFloat() else dp(16).toFloat()
        bubbleRoot.animate().alpha(1f).translationY(0f).setDuration(260).setInterpolator(OvershootInterpolator(1.1f)).start()
        reactTo("notify", 0)
        val life = if (ms > 0) ms else (4000L + text.length * 55L).coerceAtMost(20000L)
        ui.removeCallbacks(hideBubble); ui.postDelayed(hideBubble, life)
    }
    private val hideBubble = Runnable { dismissBubble() }
    private fun restoreHead() { if (savedY >= 0) { headLp.y = savedY; savedY = -1; runCatching { wm.updateViewLayout(headRoot, headLp) } } }
    private fun dismissBubble() { restoreHead(); bubbleRoot.animate().alpha(0f).setDuration(180).withEndAction { bubbleRoot.visibility = View.GONE }.start() }

    // ---- reactions: the head moves differently for each thing you do

    fun reactTo(kind: String, dir: Int) = ui.post {
        if (kind != "video") lastInput = System.currentTimeMillis()
        if (kind == "typing") lastType = System.currentTimeMillis()
        if (kind == "scroll") { val t = System.currentTimeMillis(); if (t - scrollWin > 90_000) { scrollWin = t; scrolls = 0 }; scrolls++
            if (scrolls >= 30 && t - lastDoom > 10 * 60_000) { lastDoom = t; scrolls = 0; reactTo("warn", 0)
                show("That is a lot of swiping. You are doom scrolling. Put it down.", 0, null) } }
        if (headRoot.visibility != View.VISIBLE) return@post
        val now = System.currentTimeMillis(); if (now - lastReact < 350 && kind != "notify") return@post; lastReact = now
        if (watching && kind != "video") unwatch()
        head.pivotX = head.width / 2f; head.pivotY = head.height.toFloat()
        when (kind) {
            "typing" -> head.animate().rotation(-9f).setDuration(110).withEndAction { head.animate().rotation(9f).setDuration(160).withEndAction { head.animate().rotation(0f).setDuration(120).start() }.start() }.start()
            "scroll" -> head.animate().translationY(if (dir >= 0) dp(7).toFloat() else -dp(7).toFloat()).setDuration(120).withEndAction { head.animate().translationY(0f).setDuration(220).setInterpolator(OvershootInterpolator(2f)).start() }.start()
            "click" -> head.animate().scaleX(0.84f).scaleY(0.84f).setDuration(70).withEndAction { head.animate().scaleX(1.12f).scaleY(1.12f).setDuration(120).withEndAction { head.animate().scaleX(1f).scaleY(1f).setDuration(120).start() }.start() }.start()
            "notify" -> head.animate().translationY(-dp(18).toFloat()).setDuration(120).withEndAction { head.animate().translationY(0f).setDuration(520).setInterpolator(BounceInterpolator()).start() }.start()
            "warn" -> head.animate().rotation(-14f).setDuration(70).withEndAction { head.animate().rotation(14f).setDuration(110).withEndAction { head.animate().rotation(-8f).setDuration(90).withEndAction { head.animate().rotation(0f).setDuration(90).start() }.start() }.start() }.start()
            "video" -> { watching = true }
        }
    }
    private fun unwatch() { watching = false }

    // every few seconds: are you watching a video?
    private val mood = object : Runnable { override fun run() {
        val am = getSystemService(AudioManager::class.java)
        val inVideo = videoApps.any { fgPkg.contains(it) } && System.currentTimeMillis() - lastInput > 6000 && am.isMusicActive
        if (inVideo) reactTo("video", 0) else if (watching) ui.post { unwatch() }
        ui.postDelayed(this, 3000)
    } }

    private fun summary(): String {
        val s = Vault.open(this); val open = s.promises().filter { it.status == Status.Open }
        val n = open.firstOrNull() ?: return "Nothing is owed right now. Keep it that way."
        val m = Duration.between(ZonedDateTime.now(), n.due).toMinutes()
        val busy = CalendarBridge(this).busyHours(ZonedDateTime.now())
        val load = if (busy >= 6) " Your day is packed." else ""
        return "${open.size} open. Next: ${n.what} for ${n.person} in ${if (m < 60) "$m min" else "${m / 60} h"}.$load"
    }

    // ---- deadlines and the odd word of nagging
    private val tick = object : Runnable { override fun run() { runCatching { check() }; ui.postDelayed(this, 30_000) } }
    private fun check() {
        val s = Vault.open(this); s.sweepMissed()
        val now = ZonedDateTime.now()
        s.promises().filter { it.status == Status.Open }.forEach { p ->
            val m = Duration.between(now, p.due).toMinutes()
            for (t in listOf(60, 15, 5)) if (m in 0..t.toLong() && warned.add("${p.id}:$t")) { reactTo("warn", 0)
                show("${p.person}: ${p.what}. $m min left.", 0, Intent(this, MainActivity::class.java)) }
        }
        s.promises().lastOrNull { it.status == Status.Missed && it.id !in s.debriefed() }?.let {
            if (warned.add("miss:${it.id}")) { reactTo("warn", 0); show("You missed ${it.person}'s ${it.what.lowercase()}. Come and tell me why.", 0, Intent(this, MainActivity::class.java)) }
        }
        habitsCheck()
        if (System.currentTimeMillis() - lastChatter > 25 * 60_000 && bubbleRoot.visibility != View.VISIBLE) { lastChatter = System.currentTimeMillis(); show(chatter.random(), 0, null) }
    }

    // ---- habits: what you usually do, and how long
    private fun habitsCheck() {
        val h = app.nayom.data.Habits(this); val pkg: String = Habits_cur(); val ms: Long = h.currentMs(); val min: Int = (ms / 60000).toInt()
        if (pkg != wasteApp) { wasteApp = pkg; wasteLevel = 0 }
        val name = h.label(pkg)
        if (app.nayom.data.Habits.waster(pkg) && min >= 10 * (wasteLevel + 1)) {
            wasteLevel++
            val total = h.wasterToday()
            val line = when (wasteLevel) { 1 -> "$min minutes on $name. Enough." ; 2 -> "$min minutes. I am not asking now. Close $name."
                else -> "$min minutes on $name. $total minutes wasted today. Do you want to explain this to me?" }
            reactTo("warn", 0); show(line, 0, null)
        } else if (h.opens(pkg) >= 3 && h.usualMin(pkg) > 0 && min >= h.usualMin(pkg) * 2 && min >= 5 && wasteLevel == 0 && !app.nayom.data.Habits.waster(pkg)) {
            wasteLevel = 1; show("You usually stay on $name for ${h.usualMin(pkg)} min. It is $min now.", 0, null)
        }
        val hr = java.time.LocalTime.now().hour
        if (hr != lastExpectHr) { lastExpectHr = hr
            h.expectedNow(hr)?.let { e -> if (e != pkg && app.nayom.data.Habits.waster(e)) show("Around now you open ${h.label(e)} for about ${h.usualMin(e)} min. Not today.", 0, null) } }
    }
    private fun Habits_cur(): String = app.nayom.data.Habits.cur

    // ---- calls
    private fun watchCalls() {
        runCatching {
            val tm = getSystemService(TelephonyManager::class.java)
            tm.registerTelephonyCallback(mainExecutor, object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    if (state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) {
                        hiddenByApp = false; prefs.headHidden = false; applyVisibility(); reactTo("warn", 0)
                        show("On a call? Do not say yes to anything you cannot do. ${summary()}", 0, null)
                    }
                }
            })
        }
    }

    override fun onDestroy() { instance = null; breathing?.cancel(); ui.removeCallbacksAndMessages(null); runCatching { wm.removeView(headRoot); wm.removeView(bubbleRoot) }; super.onDestroy() }
}
