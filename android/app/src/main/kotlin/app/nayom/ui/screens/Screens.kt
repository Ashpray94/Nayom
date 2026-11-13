package app.nayom.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nayom.R
import app.nayom.data.*
import app.nayom.ui.*
import app.nayom.ui.components.*
import app.nayom.ui.theme.*
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** 24 edge, 24 between ideas, one focal point, actions pinned at the bottom. */
@Composable
fun Screen(actions: (@Composable ColumnScope.() -> Unit)? = null, scroll: Boolean = false, content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = Space.edge.dp).padding(top = 16.dp, bottom = 24.dp)) {
        Column(Modifier.weight(1f).then(if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            verticalArrangement = Arrangement.spacedBy(Space.gap.dp), content = content)
        if (actions != null) { Spacer(Modifier.height(16.dp)); Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = actions) }
    }

@Composable
fun IconLine(icon: Int, tint: Color, text: String) =
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Tile(icon, tint); Text(text, style = NayomType.body, color = colors.ink, modifier = Modifier.weight(1f))
    }

private val hm = DateTimeFormatter.ofPattern("HH:mm")
private val dm = DateTimeFormatter.ofPattern("EEE d MMM, HH:mm")
fun countdown(to: ZonedDateTime, now: ZonedDateTime): String {
    val s = Duration.between(now, to).seconds
    val a = kotlin.math.abs(s)
    return (if (s < 0) "-" else "") + "%02d:%02d:%02d".format(a / 3600, a % 3600 / 60, a % 60)
}

@Composable
fun TodayScreen(st: AppState) = Screen(actions = {
    val n = st.next
    if (n != null) Button("Done", true, Modifier.fillMaxWidth()) { st.mark(n.id, Status.Kept) }
    Button("Add request", n == null && st.unhandledMiss == null, Modifier.fillMaxWidth()) { st.route = Route.AskIn }
    Row(Modifier.fillMaxWidth().clickable { st.route = Route.Evening }.padding(top = 8.dp), horizontalArrangement = Arrangement.Center) { Label("Day summary") }
}, scroll = st.next != null || st.unhandledMiss != null) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Title("Today")
    }
    val miss = st.unhandledMiss
    if (miss != null) {
        Card {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Illustration("face_stern", colors.berry, 150) }
            Text("MISSED", style = NayomType.status, color = colors.berry)
            Spacer(Modifier.height(12.dp))
            Text("${miss.person}'s ${miss.what.lowercase()} was due at ${miss.due.format(hm)}.", style = NayomType.title, color = colors.ink)
            Spacer(Modifier.height(24.dp))
            Button("Explain", true, Modifier.fillMaxWidth()) { st.route = Route.Debrief(miss.id) }
            Spacer(Modifier.height(12.dp))
            Button("Postpone", false, Modifier.fillMaxWidth()) { st.mark(miss.id, Status.Moved, st.now.plusDays(1)) }
        }
    }
    val n = st.next
    if (n != null) {
        val left = Duration.between(st.now, n.due)
        HeroDial("Next promise", countdown(n.due, st.now), n.what, "${n.person} · due ${n.due.format(hm)}",
            1f - left.seconds / 86400f, left.seconds < 3600)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Label("${st.dueSoon} due in the next 48 h") }
    } else if (miss == null) {
        Box(Modifier.fillMaxWidth().weight(1f, fill = true).verticalScroll(rememberScrollState()), Alignment.TopCenter) {
            Column(verticalArrangement = Arrangement.spacedBy(Space.gap.dp)) {
                if (st.day.kept > 0) Empty("all_kept", colors.teal, "All done.", "${st.day.kept} done today. Nothing is left. Rest well.")
                else Empty("clear_sky", colors.amber, "All clear.", "No promises are due now. Use the free time well.")
                Card {
                    Label("What to do here")
                    Spacer(Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconLine(R.drawable.ic_promises, colors.teal, "Promised someone? Add it in Promises.")
                        IconLine(R.drawable.ic_people, colors.sky, "Someone asked you for something? Tap Add request before you answer.")
                        IconLine(R.drawable.ic_today, colors.amber, "Every night, check how your day went.")
                    }
                }
            }
        }
    }
}

@Composable
fun PromisesScreen(st: AppState) = Screen(actions = {
    Button("Add promise", true, Modifier.fillMaxWidth()) { st.route = Route.NewPromise() }
}, scroll = true) {
    Title("Promises")
    val open = st.open
    if (open.isEmpty()) Empty("nothing_owed", colors.berry, "No promises.", "Your promises show here until you finish them.")
    else Card { open.forEach { p -> Row2(p.what, "${p.person} · ${p.due.format(dm)}", countdown(p.due, st.now).take(5)) { st.route = Route.PromiseDetail(p.id) } } }
    val past = st.promises.filter { it.status != Status.Open }.takeLast(8).reversed()
    if (past.isNotEmpty()) {
        Label("Earlier")
        Card { past.forEach { p -> Row2(p.what, p.person, when (p.status) { Status.Kept -> "Done"; Status.Released -> "Dropped"; else -> p.status.name }) } }
    }
}

@Composable
fun NewPromiseScreen(st: AppState, r: Route.NewPromise) {
    var text by remember { mutableStateOf(r.what) }
    var whoEdit by remember { mutableStateOf<String?>(r.who.ifBlank { null }) }
    val nowT = remember { ZonedDateTime.now() }
    val opts = listOf("In 1 hour" to nowT.plusHours(1), "Tonight 9 PM" to nowT.withHour(21).withMinute(0).withSecond(0),
        "Tomorrow 10 AM" to nowT.plusDays(1).withHour(10).withMinute(0).withSecond(0), "In 3 days" to nowT.plusDays(3))
    var pick by remember { mutableStateOf(if (r.what.isNotBlank() && Nlp.parse(r.what, nowT).due == null) r.due.coerceIn(0, 3) else -1) }
    val p = remember(text) { Nlp.parse(text, nowT) }
    val who = whoEdit ?: p.who.orEmpty()
    val due = if (pick >= 0) opts[pick].second else p.due
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM, h:mm a")
    val ok = text.isNotBlank() && who.isNotBlank() && due != null
    Screen(actions = {
        Button("Save", ok, Modifier.fillMaxWidth()) { if (ok) st.addPromise(who, p.what, due!!, r.app) }
        Button("Cancel", false, Modifier.fillMaxWidth()) { st.back() }
    }, scroll = true) {
        Title("New promise")
        Field("What did you promise?", text, { text = it; pick = -1 }, false, 90)
        Text("Write it like you talk. Example: Send the report to Rahul tomorrow at 5 pm.", style = NayomType.body, color = colors.muted)
        if (text.isNotBlank()) Card {
            Label("I understood")
            Spacer(Modifier.height(8.dp))
            Text(p.what, style = NayomType.title, color = colors.ink)
            Spacer(Modifier.height(4.dp))
            Text("For: " + who.ifBlank { "not found, please write below" }, style = NayomType.body, color = if (who.isBlank()) colors.berry else colors.muted)
            Text("When: " + (due?.format(fmt) ?: "not found, please pick below"), style = NayomType.body, color = if (due == null) colors.berry else colors.muted)
        }
        Field("Who is it for?", who, { whoEdit = it })
        Label("Change the time")
        val clash = remember(due, st.permTick) { due?.let { d -> runCatching { st.calendar.clash(d) }.getOrNull() } }
        if (clash != null) Text("Your calendar has \"${clash.title}\" at ${clash.start.format(hm)}. Pick a time you can keep.", style = NayomType.body, color = colors.berry)
        else if (st.busy >= 6) Text("Your next 12 hours are ${"%.0f".format(st.busy)} hours full. Promise carefully.", style = NayomType.body, color = colors.amber)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            opts.chunked(2).forEachIndexed { row, pair -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEachIndexed { i, o -> Chip(o.first, pick == row * 2 + i) { pick = if (pick == row * 2 + i) -1 else row * 2 + i } } } }
        }
    }
}

@Composable
fun PromiseDetailScreen(st: AppState, id: String) {
    val p = st.promises.firstOrNull { it.id == id } ?: run { st.back(); return }
    Screen(actions = {
        Button("Done", true, Modifier.fillMaxWidth()) { st.mark(id, Status.Kept) }
        Button("Postpone", false, Modifier.fillMaxWidth()) { st.mark(id, Status.Moved, p.due.plusDays(1)) }
        Button("Drop", false, Modifier.fillMaxWidth()) { st.mark(id, Status.Released) }
        Button("Back", false, Modifier.fillMaxWidth()) { st.back() }
    }) {
        Title("Promise")
        Text(p.what, style = NayomType.title, color = colors.ink)
        Card {
            Text(countdown(p.due, st.now), style = NayomType.numeral, color = if (p.due.isBefore(st.now)) colors.berry else colors.ink)
            Spacer(Modifier.height(8.dp)); Text("${p.person} · ${p.due.format(dm)}", style = NayomType.body, color = colors.muted)
        }
    }
}

@Composable
fun PeopleScreen(st: AppState) = Screen(actions = {
    if (st.people.isEmpty()) Button("Add promise", true, Modifier.fillMaxWidth()) { st.route = Route.NewPromise() }
}, scroll = true) {
    Title("People")
    if (st.people.isEmpty()) Empty("no_one_yet", colors.sky, "No one yet.", "People show here after you add a promise or a request.")
    else Card { st.people.forEach { (n, c) -> Row2(n, if (c == 0) "Nothing open" else "$c open", null) } }
}

@Composable
fun NotesScreen(st: AppState) = Screen(actions = {
    Button("Add note", true, Modifier.fillMaxWidth()) { st.route = Route.Note(null) }
}, scroll = true) {
    Title("Notes")
    if (st.notes.isEmpty()) Empty("blank_page", colors.amber, "No notes yet.", "Your notes show here. Add the first one.")
    else Card { st.notes.forEach { n -> Row2(n.name, n.file.parentFile?.name ?: "", null) { st.route = Route.Note(n.file) } } }
}

@Composable
fun NoteScreen(st: AppState, r: Route.Note) {
    if (r.file != null) {
        val text = remember(r.file) { r.file.readText() }
        Screen(actions = { Button("Back", false, Modifier.fillMaxWidth()) { st.back() } }, scroll = true) {
            Title("Note"); Text(text, style = NayomType.body, color = colors.ink)
        }
    } else {
        var t by remember { mutableStateOf("") }; var b by remember { mutableStateOf("") }
        Screen(actions = {
            Button("Save", t.isNotBlank(), Modifier.fillMaxWidth()) { if (t.isNotBlank()) st.saveNote(t, b) }
            Button("Cancel", false, Modifier.fillMaxWidth()) { st.back() }
        }, scroll = true) { Title("New note"); Field("Title", t, { t = it }); Field("Note", b, { b = it }, false, 160) }
    }
}

@Composable
fun AskInScreen(st: AppState) {
    var who by remember { mutableStateOf("") }; var what by remember { mutableStateOf("") }
    Screen(actions = {
        Button("Next", who.isNotBlank() && what.isNotBlank(), Modifier.fillMaxWidth()) {
            if (who.isNotBlank() && what.isNotBlank()) { val id = st.ask(who, what); st.route = Route.Gate(id) } }
        Button("Cancel", false, Modifier.fillMaxWidth()) { st.back() }
    }, scroll = true) { Title("Someone asked"); Field("Who", who, { who = it }); Field("What do they want", what, { what = it }) }
}

/** I-01 Gate. Yes and Later unlock after 8 s. */
@Composable
fun GateScreen(st: AppState, askId: String) {
    val a = st.store.asks().firstOrNull { it.id == askId } ?: run { st.back(); return }
    var left by remember { mutableStateOf(8) }
    LaunchedEffect(Unit) { while (left > 0) { kotlinx.coroutines.delay(1000); left-- } }
    val locked = left > 0
    fun go(d: Decision) { st.decide(askId, d); if (d == Decision.Yes) st.route = Route.NewPromise(a.from, a.what) else st.back() }
    Screen(actions = {
        Button("Say no", true, Modifier.fillMaxWidth()) { go(Decision.No) }
        Button("Wait", false, Modifier.fillMaxWidth()) { if (!locked) go(Decision.Later) }
        Button("Say yes", false, Modifier.fillMaxWidth()) { if (!locked) go(Decision.Yes) }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Label(if (locked) "Yes and Later unlock in $left s" else "Choose") }
    }, scroll = true) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Illustration("face_wry", colors.berry, 170) }
    Title("Before you say yes")
        Text("${a.from} asks: ${a.what}.", style = NayomType.title, color = colors.ink)
        val clash = st.open.firstOrNull { it.due.isBefore(st.now.plusHours(24)) }
        if (clash != null) Card { IconLine(R.drawable.ic_clock, colors.amber, "This will clash with ${clash.person}'s task: ${clash.what.lowercase()}, due ${clash.due.format(hm)}.") }
    }
}

@Composable
fun DebriefScreen(st: AppState, id: String) {
    val p = st.promises.firstOrNull { it.id == id }
    var why by remember { mutableStateOf("") }
    Screen(actions = {
        Button("Save", why.isNotBlank(), Modifier.fillMaxWidth()) { if (why.isNotBlank()) st.debrief(id, why) }
        Button("Back", false, Modifier.fillMaxWidth()) { st.back() }
    }, scroll = true) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Illustration("face_worried", colors.sky, 170) }
        Title("What happened?")
        Text("Why did you not finish: ${p?.what?.lowercase() ?: "it"}?", style = NayomType.title, color = colors.ink)
        Field("Tell me in one line", why, { why = it }, false, 120)
    }
}

/** S-08 Evening. The one serif moment. */
@Composable
fun EveningScreen(st: AppState) = Screen(actions = {
    Button("Back", false, Modifier.fillMaxWidth()) { st.back() }
}) {
    Label("Tonight")
    Text("Put the phone down.", style = NayomType.hero, color = colors.ink)
    if (st.day == Day(0, 0, 0, 0, 0)) Empty("quiet_day", colors.sky, "A quiet day.", "Nothing done, missed or moved today.")
    else Card {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(Triple("Done", st.day.kept, colors.teal), Triple("Missed", st.day.missed, colors.berry), Triple("Moved", st.day.moved, colors.ink),
                Triple("Quick yes", st.day.reflexYes, colors.ink)).forEach { (k, v, c) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$v", style = NayomType.numeral.copy(fontSize = 40.sp), color = c); Spacer(Modifier.height(8.dp)); Label(k)
                }
            }
        }
    }
    if (st.day.gated > 0) Text("${st.day.gated} asks were checked before you said yes today.", style = NayomType.body, color = colors.muted)
}

@Composable
fun SetupScreen(st: AppState) = Screen(scroll = true) {
    Title("Settings")
    Card { IconLine(R.drawable.ic_shield, colors.sky, "I never read banking, OTP and password apps.") }
    Card { IconLine(R.drawable.ic_check, colors.teal, "Everything is saved as files on this phone. Nothing leaves it.") }
    Card { Label("Notes folder"); Spacer(Modifier.height(8.dp)); Text(st.store.root.absolutePath, style = NayomType.body, color = colors.muted) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    PermissionCentre(st)
    Button("View people", false, Modifier.fillMaxWidth()) { st.route = Route.People }
    Button("View activity", false, Modifier.fillMaxWidth()) { st.loadActivity(); st.route = Route.Activity }
    Button("Replay intro", false, Modifier.fillMaxWidth()) { st.replayIntro() }
}

@Composable
fun ActivityScreen(st: AppState) = Screen(actions = { Button("Back", false, Modifier.fillMaxWidth()) { st.tab = 4; st.back() } }, scroll = true) {
    Title("Activity")
    Text("Everything you did today. Saved as a file in your vault.", style = NayomType.body, color = colors.muted)
    if (st.activity.isEmpty()) Text("Nothing yet.", style = NayomType.body, color = colors.muted)
    st.activity.forEach { e ->
        Card { Label("${e.time}  ${e.kind}"); Spacer(Modifier.height(6.dp)); Text(e.text, style = NayomType.body, color = colors.ink) }
    }
}


@Composable
fun OnboardingScreen(st: AppState, step: Int) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val pages = listOf(
        Triple("clear_sky", "I am Nayom.", "I am here so you keep your word. I will be firm, and I will be kind about it."),
        Triple("nothing_owed", "Three simple steps.", "1. Promised someone? Write it in Promises.\n2. Someone asks you for something? Tell me first, then answer.\n3. Every night, check how your day went."),
        Triple("no_one_yet", "Let me see your messages.", "I read message notifications on this phone to notice promises. Banking, OTP and password apps are never read. Everything stays on this phone."),
    )
    val (art, line, text) = pages[step]
    Screen(actions = {
        if (step == 2) Button("Allow messages", false, Modifier.fillMaxWidth()) {
            ctx.startActivity(android.content.Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        Button(if (step < 2) "Next" else "Start", true, Modifier.fillMaxWidth()) {
            if (step < 2) st.route = Route.Onboarding(step + 1) else st.finishOnboarding()
        }
    }, scroll = true) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.Center) { Mom(art, 300) }
        Text(line, style = NayomType.display.copy(fontSize = 38.sp, lineHeight = 44.sp), color = colors.ink, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text(text, style = NayomType.body, color = colors.muted, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Label("${step + 1} of 3") }
    }
}


@Composable
fun PermissionCentre(st: AppState) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { app.nayom.data.Prefs(ctx) }
    st.permTick
    val overlay = android.provider.Settings.canDrawOverlays(ctx)
    val access = android.provider.Settings.Secure.getString(ctx.contentResolver, android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES).orEmpty().contains(ctx.packageName)
    val listener = androidx.core.app.NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)
    val cal = st.calendar.allowed
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) { st.permTick++ }
    fun open(action: String, uri: String? = null) = ctx.startActivity(android.content.Intent(action).apply { if (uri != null) data = android.net.Uri.parse(uri) }.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
    Label("What Nayom can do")
    Card {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            PermRow("Floating head over apps", overlay, "Allow") { open(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${ctx.packageName}") }
            PermRow("See what I type", access, "Allow") { open(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS) }
            PermRow("See my messages", listener, "Allow") { open("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS") }
            PermRow("Calendar, calls and notifications", cal, "Allow") {
                launcher.launch(arrayOf(android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR,
                    android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.POST_NOTIFICATIONS))
            }
        }
    }
    if (!access) Text("If Allow is greyed out: App info for Nayom, three-dot menu, Allow restricted settings. Then try again.", style = NayomType.body, color = colors.muted)
    var on by remember { mutableStateOf(prefs.avatarOn) }
    var hidden by remember { mutableStateOf(prefs.headHidden) }
    Button(if (hidden) "Show head" else "Hide head", false, Modifier.fillMaxWidth()) {
        hidden = !hidden; prefs.headHidden = hidden
        if (overlay) app.nayom.service.OverlayService.command(ctx, if (hidden) "HIDE" else "SHOW")
    }
    Text("Hiding only clears the screen. Nayom keeps watching.", style = NayomType.body, color = colors.muted)
}

@Composable
private fun PermRow(label: String, on: Boolean, cta: String, go: () -> Unit) =
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) { Text(label, style = NayomType.body, color = colors.ink) }
        if (on) Label("On") else Box(Modifier.clickable(onClick = go).padding(8.dp)) { Label(cta) }
    }


@Composable
fun InboxScreen(st: AppState) = Screen(actions = { Button("Back", false, Modifier.fillMaxWidth()) { st.back() } }, scroll = true) {
    Title("Messages")
    val msgs = remember { st.store.inboxToday() }
    if (msgs.isEmpty()) Text("Nothing has reached this phone yet today. Check Settings: See my messages.", style = NayomType.body, color = colors.muted)
    msgs.forEach { m -> Card { Label("${m.time}  ${m.app}  ${m.from}"); Spacer(Modifier.height(6.dp)); Text(m.text, style = NayomType.body, color = colors.ink) } }
}
