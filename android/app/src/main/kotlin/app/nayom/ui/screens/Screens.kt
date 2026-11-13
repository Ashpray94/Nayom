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
    if (n != null) Button("Kept", true, Modifier.fillMaxWidth()) { st.mark(n.id, Status.Kept) }
    Button("Someone asked me", n == null && st.unhandledMiss == null, Modifier.fillMaxWidth()) { st.route = Route.AskIn }
}, scroll = true) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Title("Today")
        Box(Modifier.clickable { st.route = Route.Setup }.padding(top = 16.dp)) { Label("Setup") }
    }
    val miss = st.unhandledMiss
    if (miss != null) {
        Card {
            Text("MISSED", style = NayomType.status, color = colors.berry)
            Spacer(Modifier.height(12.dp))
            Text("${miss.person}'s ${miss.what.lowercase()} was due at ${miss.due.format(hm)}.", style = NayomType.title, color = colors.ink)
            Spacer(Modifier.height(24.dp))
            Button("Debrief", true, Modifier.fillMaxWidth()) { st.route = Route.Debrief(miss.id) }
            Spacer(Modifier.height(12.dp))
            Button("Reschedule", false, Modifier.fillMaxWidth()) { st.mark(miss.id, Status.Moved, st.now.plusDays(1)) }
        }
    }
    val n = st.next
    if (n != null) {
        val left = Duration.between(st.now, n.due)
        HeroDial("Next promise", countdown(n.due, st.now), n.what, "${n.person} · due ${n.due.format(hm)}",
            1f - left.seconds / 86400f, left.seconds < 3600)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Label("${st.dueSoon} due in the next 48 h") }
    } else if (miss == null) {
        if (st.day.kept > 0) Empty("all_kept", colors.teal, "All kept.", "${st.day.kept} kept today. Nothing is owed. Rest well.")
        else Empty("clear_sky", colors.amber, "Clear sky.", "No promises due. When you say yes to someone, add it in Promises.")
    }
    Box(Modifier.clickable { st.route = Route.Evening }) { Label("Evening sheet") }
}

@Composable
fun PromisesScreen(st: AppState) = Screen(actions = {
    Button("New promise", true, Modifier.fillMaxWidth()) { st.route = Route.NewPromise() }
}, scroll = true) {
    Title("Promises")
    val open = st.open
    if (open.isEmpty()) Empty("nothing_owed", colors.berry, "Nothing owed.", "Every yes you give lives here until you keep it.")
    else Card { open.forEach { p -> Row2(p.what, "${p.person} · ${p.due.format(dm)}", countdown(p.due, st.now).take(5)) { st.route = Route.PromiseDetail(p.id) } } }
    val past = st.promises.filter { it.status != Status.Open }.takeLast(8).reversed()
    if (past.isNotEmpty()) {
        Label("Earlier")
        Card { past.forEach { p -> Row2(p.what, p.person, p.status.name) } }
    }
}

@Composable
fun NewPromiseScreen(st: AppState, r: Route.NewPromise) {
    var who by remember { mutableStateOf(r.who) }; var what by remember { mutableStateOf(r.what) }
    val opts = listOf("In 1 hour" to st.now.plusHours(1), "Tonight 21:00" to st.now.withHour(21).withMinute(0).withSecond(0),
        "Tomorrow 10:00" to st.now.plusDays(1).withHour(10).withMinute(0).withSecond(0), "In 3 days" to st.now.plusDays(3))
    var due by remember { mutableStateOf(1) }
    Screen(actions = {
        Button("Save promise", who.isNotBlank() && what.isNotBlank(), Modifier.fillMaxWidth()) {
            if (who.isNotBlank() && what.isNotBlank()) st.addPromise(who, what, opts[due].second) }
        Button("Cancel", false, Modifier.fillMaxWidth()) { st.back() }
    }, scroll = true) {
        Title("New promise")
        Field("To whom", who, { who = it }); Field("What", what, { what = it })
        Label("By when")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            opts.chunked(2).forEachIndexed { row, pair -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEachIndexed { i, o -> Chip(o.first, due == row * 2 + i) { due = row * 2 + i } } } }
        }
    }
}

@Composable
fun PromiseDetailScreen(st: AppState, id: String) {
    val p = st.promises.firstOrNull { it.id == id } ?: run { st.back(); return }
    Screen(actions = {
        Button("Kept", true, Modifier.fillMaxWidth()) { st.mark(id, Status.Kept) }
        Button("Move to tomorrow", false, Modifier.fillMaxWidth()) { st.mark(id, Status.Moved, p.due.plusDays(1)) }
        Button("Release", false, Modifier.fillMaxWidth()) { st.mark(id, Status.Released) }
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
    if (st.people.isEmpty()) Button("New promise", true, Modifier.fillMaxWidth()) { st.route = Route.NewPromise() }
}, scroll = true) {
    Title("People")
    if (st.people.isEmpty()) Empty("no_one_yet", colors.sky, "No one yet.", "People appear here once you add a promise or log an ask.")
    else Card { st.people.forEach { (n, c) -> Row2(n, if (c == 0) "Nothing open" else "$c open", null) } }
}

@Composable
fun NotesScreen(st: AppState) = Screen(actions = {
    Button("New note", true, Modifier.fillMaxWidth()) { st.route = Route.Note(null) }
}, scroll = true) {
    Title("Notes")
    if (st.notes.isEmpty()) Empty("blank_page", colors.amber, "Blank page.", "Markdown files in your vault show up here. Write the first one.")
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
        Button("Continue", who.isNotBlank() && what.isNotBlank(), Modifier.fillMaxWidth()) {
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
        Button("No, kindly", true, Modifier.fillMaxWidth()) { go(Decision.No) }
        Button("Later", false, Modifier.fillMaxWidth()) { if (!locked) go(Decision.Later) }
        Button("Yes", false, Modifier.fillMaxWidth()) { if (!locked) go(Decision.Yes) }
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Label(if (locked) "Yes and Later unlock in $left s" else "Choose") }
    }) {
        Title("Before you say yes")
        Text("${a.from} asks: ${a.what}.", style = NayomType.title, color = colors.ink)
        val clash = st.open.firstOrNull { it.due.isBefore(st.now.plusHours(24)) }
        if (clash != null) Card { IconLine(R.drawable.ic_clock, colors.amber, "Pushes out ${clash.person}'s ${clash.what.lowercase()}, due ${clash.due.format(hm)}.") }
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
        Title("Debrief")
        Text("What got in the way of ${p?.what?.lowercase() ?: "it"}?", style = NayomType.title, color = colors.ink)
        Field("One line, honest", why, { why = it }, false, 120)
    }
}

/** S-08 Evening. The one serif moment. */
@Composable
fun EveningScreen(st: AppState) = Screen(actions = {
    Button("Back", false, Modifier.fillMaxWidth()) { st.back() }
}) {
    Label("Evening")
    Text("Phone down.", style = NayomType.hero, color = colors.ink)
    if (st.day == Day(0, 0, 0, 0, 0)) Empty("quiet_day", colors.sky, "A quiet day.", "Nothing kept, missed or moved today.")
    else Card {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(Triple("Kept", st.day.kept, colors.teal), Triple("Missed", st.day.missed, colors.berry), Triple("Moved", st.day.moved, colors.ink),
                Triple("Reflex yes", st.day.reflexYes, colors.ink)).forEach { (k, v, c) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$v", style = NayomType.numeral.copy(fontSize = 40.sp), color = c); Spacer(Modifier.height(8.dp)); Label(k)
                }
            }
        }
    }
    if (st.day.gated > 0) Text("${st.day.gated} asks went through the gate today.", style = NayomType.body, color = colors.muted)
}

@Composable
fun SetupScreen(st: AppState) = Screen(actions = { Button("Back", false, Modifier.fillMaxWidth()) { st.back() } }, scroll = true) {
    Title("Setup")
    Card { IconLine(R.drawable.ic_shield, colors.sky, "Never read: banking, OTP, authenticator and password apps.") }
    Card { IconLine(R.drawable.ic_check, colors.teal, "Everything is saved as files on this phone. Nothing leaves it.") }
    Card { Label("Vault folder"); Spacer(Modifier.height(8.dp)); Text(st.store.root.absolutePath, style = NayomType.body, color = colors.muted) }
}
