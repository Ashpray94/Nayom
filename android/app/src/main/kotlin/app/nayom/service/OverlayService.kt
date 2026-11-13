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
        fun say(text: String, ms: Long = 0, tap: Intent? = null) { instance?.show(text, ms, tap, false) }
        fun appHidden(h: Boolean) { instance?.setAppHidden(h) }
        fun mood(face: String, ms: Long = 3000) { instance?.ui?.post { instance?.setFace(face, ms) } }
        fun react(kind: String, dir: Int = 0) { instance?.reactTo(kind, dir) }
        fun look(x: Int, y: Int) { instance?.lookAt(x, y) }
        fun typing(l: Int, t: Int, r: Int, b: Int, before: String) { instance?.typingAt(l, t, r, b, before) }
        fun fixes(list: List<String>) { instance?.showFixes(list) }
        fun start(ctx: Context) { ctx.startForegroundService(Intent(ctx, OverlayService::class.java)) }
        fun command(ctx: Context, action: String) { ctx.startForegroundService(Intent(ctx, OverlayService::class.java).setAction(action)) }
    }

    private lateinit var wm: WindowManager
    private lateinit var headRoot: LinearLayout
    private lateinit var head: ImageView
    private lateinit var actionsRoot: LinearLayout
    private lateinit var actionsLp: WindowManager.LayoutParams
    private lateinit var scroll: ScrollView
    private var anger = 0
    private var scrollLvl = 0
    private var swipes = 0; private var swipePkg = ""; private var lastSwipe = 0L
    private var counter: TextView? = null
    private var face = "calm"
    private val faceRes = mapOf("calm" to R.drawable.head_calm, "happy" to R.drawable.head_happy, "stern" to R.drawable.head_stern,
        "angry" to R.drawable.head_angry, "rage" to R.drawable.head_rage, "sad" to R.drawable.head_sad,
        "dl" to R.drawable.head_look_dl, "dc" to R.drawable.head_look_dc, "dr" to R.drawable.head_look_dr, "worried" to R.drawable.head_worried, "sleepy" to R.drawable.head_sleepy)
    private fun baseFace(): String { val h = java.time.LocalTime.now().hour
        val e = maxOf(anger, scrollLvl); return if (e >= 3) "rage" else if (e >= 2) "angry" else if (e == 1) "stern" else if (h >= 23 || h < 5) "sleepy" else "calm" }
    /** Swaps the face with a quick cross-fade; returns to her resting face after holdMs. */
    fun setFace(name: String, holdMs: Long = 0) {
        if (name != face) {
            val from = head.drawable; val to = resources.getDrawable(faceRes.getValue(name), null)
            head.setImageDrawable(android.graphics.drawable.TransitionDrawable(arrayOf(from, to)).also { it.isCrossFadeEnabled = true; it.startTransition(160) })
            face = name
        }
        ui.removeCallbacks(restFace)
        if (holdMs > 0) ui.postDelayed(restFace, holdMs)
    }
    private val restFace = Runnable { setFace(baseFace()) }
    private val blink = object : Runnable { override fun run() {
        if (face == "calm" || face == "stern") { val f = face; setFace("sleepy"); ui.postDelayed({ if (face == "sleepy") setFace(f) }, 130) }
        ui.postDelayed(this, 3500L + (Math.random() * 3500).toLong()) } }
    private lateinit var headLp: WindowManager.LayoutParams
    private lateinit var bubbleRoot: FrameLayout
    private lateinit var bubbleText: TextView
    private lateinit var bubbleLp: WindowManager.LayoutParams
    private var bubbleUp = false
    private lateinit var prefs: Prefs
    internal val ui = Handler(Looper.getMainLooper())
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
    private var savedX = 0
    private var typeMode = false
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
        build(); watchCalls(); applyVisibility(); ui.postDelayed(tick, 5000); ui.postDelayed(mood, 3000); ui.postDelayed(blink, 4000)
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
        text = label.uppercase(); setTextColor(fg); textSize = 10f; typeface = Typeface.DEFAULT_BOLD; letterSpacing = 0.1f
        setPadding(dp(10), dp(9), dp(10), dp(9)); gravity = Gravity.CENTER
        background = GradientDrawable().apply { setColor(bg); cornerRadius = dp(20).toFloat(); setStroke(dp(1), 0x330F2E2C) }
        elevation = dp(4).toFloat(); setOnClickListener { click() }
        setSingleLine(true); layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginStart = dp(6) }
    }

    private fun build() {
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        // head + tap menu
        head = ImageView(this).apply { setImageResource(R.drawable.head_calm); elevation = dp(8).toFloat()
            layoutParams = LinearLayout.LayoutParams(dp(64), dp(64)) }
        badge = TextView(this).apply { setTextColor(0xFFFFFFFF.toInt()); textSize = 12f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; visibility = View.GONE
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(0xFFD93025.toInt()); setStroke(dp(2), 0xFFFFFFFF.toInt()) }; elevation = dp(10).toFloat() }
counter = TextView(this).apply { setTextColor(0xFFFFFFFF.toInt()); textSize = 13f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER; visibility = View.GONE
            setPadding(dp(8), dp(2), dp(8), dp(2)); minWidth = dp(28); elevation = dp(10).toFloat() }
        val headFrame = FrameLayout(this).apply { addView(head); addView(badge, FrameLayout.LayoutParams(dp(24), dp(24), Gravity.TOP or Gravity.END))
            addView(counter, FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM or Gravity.START)) }
        headRoot = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.END; addView(headFrame) }
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
                    if (moved) { headLp.x = ox + dx; headLp.y = oy + dy; wm.updateViewLayout(headRoot, headLp); placeBubble() }
                }
                MotionEvent.ACTION_UP -> {
                    if (moved) { prefs.avatarX = headLp.x; prefs.avatarY = headLp.y }
                    else { reactTo("click", 0); if (bubbleRoot.visibility == View.VISIBLE && actionsOn) dismissBubble() else show(summary(), 9000, null, true) }
                }
            }
            true
        }
        wm.addView(headRoot, headLp)

        // bubble: its own wide window, scrollable, up to half the screen height
        val dm = resources.displayMetrics
        bubbleText = TextView(this).apply { setTextColor(0xFF0F2E2C.toInt()); textSize = 17f; setLineSpacing(0f, 1.15f); setPadding(dp(20), dp(18), dp(20), dp(18)) }
        scroll = ScrollView(this).apply { addView(bubbleText); isVerticalScrollBarEnabled = false }
        actionsRoot = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.END; visibility = View.GONE; setPadding(dp(4), dp(2), dp(4), dp(2))
            addView(pill("Open app") { dismissBubble(); startActivity(Intent(this@OverlayService, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) })
            addView(pill("Messages") { dismissBubble(); startActivity(Intent(this@OverlayService, MainActivity::class.java).putExtra("route", "inbox").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) })
            addView(pill("Hide head", 0xFFFFFFFF.toInt(), 0xFF22706A.toInt()) { dismissBubble(); prefs.headHidden = true; applyVisibility(); refreshNotification() }) }
        fixCol = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; visibility = View.GONE; setPadding(0, 0, 0, dp(10)) }
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(scroll); addView(fixCol) }
        bubbleRoot = FrameLayout(this).apply {
            background = GradientDrawable().apply { setColor(0xFFFFFFFF.toInt()); cornerRadius = dp(24).toFloat(); setStroke(dp(1), 0x330F2E2C) }
            elevation = dp(10).toFloat(); visibility = View.GONE; addView(col)
        }
        bubbleText.setOnClickListener { if (!actionsOn) { tapIntent?.let { i -> startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)) }; dismissBubble() } }
        bubbleLp = WindowManager.LayoutParams(dm.widthPixels - dp(32), WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
        wm.addView(bubbleRoot, bubbleLp)
        actionsLp = WindowManager.LayoutParams(dm.widthPixels - dp(32), WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL }
        wm.addView(actionsRoot, actionsLp)
    }

    // ---- show/hide
    private fun applyVisibility() = ui.post {
        val vis = if (hiddenByApp || prefs.headHidden) View.GONE else View.VISIBLE
        headRoot.visibility = vis
        if (vis == View.GONE) { bubbleRoot.visibility = View.GONE; actionsRoot.visibility = View.GONE }
    }
    fun setAppHidden(h: Boolean) { if (h != hiddenByApp) { hiddenByApp = h; applyVisibility() } }

    // ---- bubble
    private var actionsOn = false
    private var bubbleH = 0
    fun show(text: String, ms: Long, tap: Intent?, actions: Boolean = false): Boolean = ui.post {
        if (hiddenByApp || prefs.headHidden) return@post
        tapIntent = tap; actionsOn = actions; bubbleText.text = text
        if (!text.contains("mistake")) fixCol.visibility = View.GONE
        val dm = resources.displayMetrics; val maxH = dm.heightPixels / 2
        bubbleText.measure(View.MeasureSpec.makeMeasureSpec(dm.widthPixels - dp(32), View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED)
        scroll.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, minOf(bubbleText.measuredHeight, maxH))
        bubbleRoot.visibility = View.VISIBLE
        placeBubble()
        bubbleRoot.alpha = 0f
        bubbleRoot.animate().alpha(1f).setDuration(220).start()
        reactTo("notify", 0)
        val life = if (ms > 0) ms else (4000L + text.length * 55L).coerceAtMost(20000L)
        ui.removeCallbacks(hideBubble); ui.postDelayed(hideBubble, life)
    }
    /** Keeps the bubble glued to the head: below it when the head is high, above it when low. */
    private fun placeBubble() {
        if (bubbleRoot.visibility != View.VISIBLE) return
        val dm = resources.displayMetrics
        val below = typeMode || headLp.y < dm.heightPixels / 2
        bubbleRoot.measure(View.MeasureSpec.makeMeasureSpec(dm.widthPixels - dp(32), View.MeasureSpec.EXACTLY), View.MeasureSpec.UNSPECIFIED)
        val h = bubbleRoot.measuredHeight; val ah = if (actionsOn) dp(52) else 0
        val by: Int; val ay: Int
        if (typeMode) { by = dp(36); ay = by + h + dp(8) }
        else if (below) { by = headLp.y + dp(70); ay = by + h + dp(8) }
        else { by = maxOf(dp(24) + ah, headLp.y - h - dp(6)); ay = by - ah }
        bubbleLp.y = by; runCatching { wm.updateViewLayout(bubbleRoot, bubbleLp) }
        // the options live in their own strip, never inside the message box
        actionsRoot.visibility = if (actionsOn) View.VISIBLE else View.GONE
        if (actionsOn) { actionsLp.y = ay; runCatching { wm.updateViewLayout(actionsRoot, actionsLp) } }
    }
    private val hideBubble = Runnable { dismissBubble() }
    
    private fun dismissBubble() { actionsRoot.visibility = View.GONE; bubbleRoot.animate().alpha(0f).setDuration(180).withEndAction { bubbleRoot.visibility = View.GONE }.start() }

    // ---- reactions: the head moves differently for each thing you do

    fun reactTo(kind: String, dir: Int): Boolean = ui.post {
        if (kind != "video") lastInput = System.currentTimeMillis()
        if (kind == "typing") lastType = System.currentTimeMillis()
        if (kind == "scroll") { val tt = System.currentTimeMillis()
            if (fgPkg != swipePkg || tt - lastSwipe > 5 * 60_000) { swipePkg = fgPkg; swipes = 0 }
            lastSwipe = tt; swipes++
            scrollLvl = if (swipes >= 40) 3 else if (swipes >= 20) 2 else if (swipes >= 8) 1 else 0
            counter?.apply { text = swipes.toString(); visibility = if (swipes >= 3) View.VISIBLE else View.GONE
                background = GradientDrawable().apply { cornerRadius = dp(12).toFloat(); setColor(if (scrollLvl >= 2) 0xFFD93025.toInt() else if (scrollLvl == 1) 0xFFE8872E.toInt() else 0xFF22706A.toInt()); setStroke(dp(2), 0xFFFFFFFF.toInt()) } }
            if (typeMode.not()) setFace(baseFace())
            val t = tt; if (t - scrollWin > 90_000) { scrollWin = t; scrolls = 0 }; scrolls++
            if (scrolls >= 12 && t - lastDoom > 10 * 60_000) { lastDoom = t; scrolls = 0; reactTo("warn", 0)
                show("That is a lot of swiping. You are doom scrolling. Put it down.", 0, null); setAnger(3)
                runCatching { Vault.open(this).log("nag", "doomscroll in ${app.nayom.data.Habits.cur}") } } }
        if (headRoot.visibility != View.VISIBLE) return@post
        val now = System.currentTimeMillis(); if (now - lastReact < 350 && kind != "notify") return@post; lastReact = now
        if (watching && kind != "video") unwatch()
        head.pivotX = head.width / 2f; head.pivotY = head.height * 0.7f
        head.cameraDistance = 12000f * resources.displayMetrics.density
        fun pose(ry: Float, rx: Float, rz: Float = 0f, ms: Long = 160, back: Long = 700) {
            head.animate().rotationY(ry).rotationX(rx).rotation(rz).setDuration(ms).setInterpolator(android.view.animation.DecelerateInterpolator())
                .withEndAction { head.animate().rotationY(0f).rotationX(0f).rotation(0f).setStartDelay(back).setDuration(380).start() }.start()
        }
        when (kind) {
            "scroll", "nod" -> pose(0f, if (dir >= 0) -20f else 20f, 0f, 140, 450)          // nods up/down with the content
            "notify" -> { setFace("worried", 1600); pose(0f, -24f, 0f, 200, 900) }                                  // looks up at the notification
            "warn" -> { setFace(if (anger >= 3) "rage" else if (anger >= 2) "angry" else "stern", 2400); head.animate().rotationY(-26f).setDuration(150).withEndAction { head.animate().rotationY(26f).setDuration(260).withEndAction { head.animate().rotationY(-18f).setDuration(220).withEndAction { head.animate().rotationY(0f).setDuration(260).start() }.start() }.start() }.start() } // slow "no"
            "video" -> { watching = true; head.animate().rotationY(0f).rotationX(8f).setDuration(500).start() }
        }
    }
    /** Looks toward where you touched. */
    fun lookAt(x: Int, y: Int) = ui.post {
        if (headRoot.visibility != View.VISIBLE || typeMode) return@post
        val dm = resources.displayMetrics
        val cx = dm.widthPixels - headLp.x - dp(32); val cy = headLp.y + dp(32)
        val dx = ((x - cx).toFloat() / (dm.widthPixels / 2f)).coerceIn(-1f, 1f); val dy = ((y - cy).toFloat() / (dm.heightPixels / 2f)).coerceIn(-1f, 1f)
        head.cameraDistance = 12000f * dm.density
        head.pivotX = head.width / 2f; head.pivotY = head.height * 0.7f
        head.animate().rotationY(dx * 30f).rotationX(-dy * 22f).setDuration(150)
            .withEndAction { head.animate().rotationY(0f).rotationX(0f).setStartDelay(800).setDuration(400).start() }.start()
    }
    fun setAnger(level: Int) = ui.post {
        if (level == anger) return@post; anger = level
        head.colorFilter = when (level) { 0 -> null
            1, 2 -> null
            else -> null }
        setFace(baseFace())
        if (level >= 2) reactTo("warn", 0)
    }
    /** She sits 20dp off the top-left corner of the text box; her eyes sweep left-to-right with the cursor across the box. */
    private var lastLine = 0
    private var anchorL = 0; private var anchorT = 0
    fun typingAt(l: Int, t: Int, r: Int, b: Int, before: String) = ui.post {
        if (headRoot.visibility != View.VISIBLE) return@post
        val dm = resources.displayMetrics
        val hw = dp(64)
        // anchor once per text box; growing/scrolling text must not make her jump
        val newBox = !typeMode || kotlin.math.abs(t - anchorT) > dp(160) || kotlin.math.abs(l - anchorL) > dp(60)
        if (newBox) {
            if (!typeMode) { typeMode = true; savedX = headLp.x; savedY = headLp.y }
            anchorL = l; anchorT = t
            val left = (l - hw - dp(20)).coerceAtLeast(dp(4))
            headLp.x = (dm.widthPixels - left - hw).coerceIn(0, dm.widthPixels - hw)
            headLp.y = maxOf(dp(8), t - hw - dp(20))
            runCatching { wm.updateViewLayout(headRoot, headLp) }
        }
        val perLine = maxOf(8, (r - l - dp(32)) / dp(9))
        val lines = before.split('\n'); val col = lines.last().length % perLine
        val f = ((dp(16) + col * dp(9)).toFloat() / maxOf(1, r - l)).coerceIn(0f, 1f)   // 0 = left-most of the box, 1 = right-most
        val newLine = lines.size - 1 != lastLine; lastLine = lines.size - 1
        head.cameraDistance = 12000f * dm.density
        val look = f * 2f - 1f
        head.animate().rotationY(look * 16f).translationX(look * dp(4)).setDuration(110).start()
        // a new line drops her eyes straight down for a moment, then the next characters pull them back to the cursor
        setFace(if (newLine && col < 3) "dc" else if (f < 0.33f) "dl" else if (f > 0.66f) "dr" else "dc", 1800)
        ui.removeCallbacks(backToMe); ui.postDelayed(backToMe, 1800)
    }
    // ---- mistakes: red numbered badges
    private var badge: TextView? = null
    private lateinit var fixCol: LinearLayout
    fun showFixes(list: List<String>) = ui.post {
        badge?.apply { visibility = if (list.isEmpty()) View.GONE else View.VISIBLE; text = list.size.toString() }
        fixCol.removeAllViews()
        if (list.isEmpty() || headRoot.visibility != View.VISIBLE) return@post
        list.forEachIndexed { i, m ->
            fixCol.addView(LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(4), dp(16), dp(4))
                addView(TextView(context).apply { text = "${i + 1}"; setTextColor(0xFFFFFFFF.toInt()); textSize = 12f; typeface = Typeface.DEFAULT_BOLD; gravity = Gravity.CENTER
                    background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(0xFFD93025.toInt()) }; layoutParams = LinearLayout.LayoutParams(dp(22), dp(22)) })
                addView(TextView(context).apply { text = m; setTextColor(0xFF0F2E2C.toInt()); textSize = 15f; setPadding(dp(10), 0, 0, 0) }) })
        }
        fixCol.visibility = View.VISIBLE
        show("${list.size} mistake${if (list.size > 1) "s" else ""}. Fix them before you send.", 9000, null)
        setFace("stern", 3000)
    }
    private val backToMe = Runnable {
        if (!typeMode) return@Runnable
        typeMode = false
        head.animate().rotationY(0f).translationX(0f).setDuration(350).start(); setFace(baseFace())
        headLp.x = savedX; headLp.y = savedY; runCatching { wm.updateViewLayout(headRoot, headLp) }
    }
    private fun unwatch() { watching = false; head.animate().rotationX(0f).setDuration(300).start() }

    // every few seconds: are you watching a video?
    private val mood = object : Runnable { override fun run() {
        val am = getSystemService(AudioManager::class.java)
        val inVideo = videoApps.any { fgPkg.contains(it) } && System.currentTimeMillis() - lastInput > 6000 && am.isMusicActive
        if (inVideo) reactTo("video", 0) else if (watching) ui.post { unwatch() }
        angerCheck()
        if (fgPkg != swipePkg && swipes > 0) { swipes = 0; scrollLvl = 0; counter?.visibility = View.GONE; setFace(baseFace()) }
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
            if (warned.add("miss:${it.id}")) { reactTo("warn", 0); setFace("sad", 6000); show("You missed ${it.person}'s ${it.what.lowercase()}. Come and tell me why.", 0, Intent(this, MainActivity::class.java)) }
        }
        habitsCheck()
        if (System.currentTimeMillis() - lastChatter > 25 * 60_000 && bubbleRoot.visibility != View.VISIBLE) { lastChatter = System.currentTimeMillis(); show(chatter.random(), 0, null) }
    }

    private fun angerCheck() {
        val pkg: String = app.nayom.data.Habits.cur; val min = (app.nayom.data.Habits(this).currentMs() / 60000).toInt()
        var l = if (app.nayom.data.Habits.waster(pkg)) (if (min >= 20) 3 else if (min >= 8) 2 else if (min >= 3) 1 else 0) else 0
        if (System.currentTimeMillis() - lastDoom < 120_000) l = 3
        setAnger(l)
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
            reactTo("warn", 0); show(line, 0, null); runCatching { Vault.open(this).log("nag", line) }
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

    override fun onDestroy() { instance = null; breathing?.cancel(); ui.removeCallbacksAndMessages(null); runCatching { wm.removeView(headRoot); wm.removeView(bubbleRoot); wm.removeView(actionsRoot) }; super.onDestroy() }
}
