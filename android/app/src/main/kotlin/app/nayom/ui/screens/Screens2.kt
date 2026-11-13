package app.nayom.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import app.nayom.data.*
import app.nayom.ui.*
import app.nayom.ui.components.*
import app.nayom.ui.theme.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/** Talk to her. She answers from what she knows about your day. */
@Composable
fun ChatScreen(st: AppState) {
    var text by remember { mutableStateOf("") }
    val scroll = rememberScrollState()
    LaunchedEffect(Unit) { st.loadChat() }
    LaunchedEffect(st.chat.size) { scroll.animateScrollTo(scroll.maxValue) }
    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding().padding(horizontal = Space.edge.dp).padding(top = 16.dp, bottom = 12.dp)) {
        Title("Mother", warm = true)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.weight(1f).verticalScroll(scroll), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            st.chat.forEach { m ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (m.me) Arrangement.End else Arrangement.Start) {
                    Box(Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(18.dp)).background(if (m.me) colors.teal else colors.surface).padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(m.text, style = NayomType.body, color = if (m.me) Color.White else colors.ink)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        st.chatDraft?.let { d -> Button("Save promise", true, Modifier.fillMaxWidth()) { st.chatDraft = null; st.route = Route.NewPromise(what = d) }; Spacer(Modifier.height(8.dp)) }
        st.chatDone?.let { id -> Button("Mark done", true, Modifier.fillMaxWidth()) { st.chatDone = null; st.mark(id, Status.Kept) }; Spacer(Modifier.height(8.dp)) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicTextField(text, { text = it }, textStyle = NayomType.body.copy(color = colors.ink), cursorBrush = SolidColor(colors.teal),
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(24.dp)).background(colors.surface).padding(horizontal = 18.dp, vertical = 14.dp),
                decorationBox = { inner -> if (text.isEmpty()) Text("Write to Mother", style = NayomType.body, color = colors.muted); inner() })
            Box(Modifier.width(96.dp)) { Button("Send", text.isNotBlank(), Modifier.fillMaxWidth()) { if (text.isNotBlank()) { st.say(text.trim()); text = "" } } }
        }
    }
}

/** When a promise is due she asks if it is done, then helps you tell the same person, in the same app. */
@Composable
fun CheckInScreen(st: AppState, id: String) {
    val p = st.promises.firstOrNull { it.id == id } ?: run { st.back(); return }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val appName = remember(p.app) { if (p.app.contains('.')) Habits(ctx).label(p.app) else null }
    val nowT = remember { ZonedDateTime.now() }
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM, h:mm a")
    val later = listOf("In 1 hour" to nowT.plusHours(1), "Tonight 9 PM" to nowT.withHour(21).withMinute(0).withSecond(0).let { if (it.isBefore(nowT)) it.plusDays(1) else it },
        "Tomorrow 10 AM" to nowT.plusDays(1).withHour(10).withMinute(0).withSecond(0))
    var stage by remember { mutableStateOf(0) }; var outcome by remember { mutableStateOf("done") }; var pick by remember { mutableStateOf(0) }
    var msg by remember { mutableStateOf("") }
    fun draft(o: String, d: ZonedDateTime) = when (o) {
        "done" -> "Hi ${p.person}, this is done: ${p.what}."
        "later" -> "Hi ${p.person}, I need a little more time for: ${p.what}. I will finish by ${d.format(fmt)}."
        else -> "Hi ${p.person}, sorry, I cannot do this: ${p.what}. Please plan without me."
    }
    fun commit() = when (outcome) { "done" -> st.markStay(id, Status.Kept); "later" -> st.markStay(id, Status.Moved, later[pick].second); else -> st.markStay(id, Status.Released) }
    if (stage == 0) Screen(actions = {
        Button("Done", true, Modifier.fillMaxWidth()) { outcome = "done"; msg = draft("done", later[0].second); stage = 1 }
        Button("Need more time", false, Modifier.fillMaxWidth()) { outcome = "later"; msg = draft("later", later[0].second); stage = 1 }
        Button("Cannot do", false, Modifier.fillMaxWidth()) { outcome = "cannot"; msg = draft("cannot", later[0].second); stage = 1 }
    }) {
        Title("Time is up")
        Text("Did you finish: ${p.what}?", style = NayomType.title, color = colors.ink)
        Card { Text("For ${p.person}. It was due ${p.due.format(fmt)}.", style = NayomType.body, color = colors.muted) }
    } else Screen(actions = {
        Button("Send", msg.isNotBlank(), Modifier.fillMaxWidth()) { commit(); st.tellThem(p, msg) ; st.back() }
        Button("Skip", false, Modifier.fillMaxWidth()) { commit(); st.back() }
    }, scroll = true) {
        Title("Tell ${p.person}")
        Text(if (appName != null) "This opens $appName so you can send it to ${p.person}." else "This opens your share list. Choose ${p.person}.", style = NayomType.body, color = colors.muted)
        if (outcome == "later") {
            Label("New time")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { later.forEachIndexed { i, o -> Chip(o.first, pick == i) { pick = i; msg = draft("later", o.second) } } }
        }
        Field("Message", msg, { msg = it }, false, 110)
    }
}
