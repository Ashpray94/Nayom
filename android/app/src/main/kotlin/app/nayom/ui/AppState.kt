package app.nayom.ui

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.nayom.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZonedDateTime

sealed interface Route {
    data object Home : Route
    data class PromiseDetail(val id: String) : Route
    data class NewPromise(val who: String = "", val what: String = "", val due: Int = 1, val app: String = "nayom") : Route
    data object AskIn : Route
    data class Gate(val askId: String) : Route
    data class Debrief(val id: String) : Route
    data class Note(val file: java.io.File?) : Route
    data object Evening : Route
    data object Setup : Route
    data object Activity : Route
    data class CheckIn(val id: String) : Route
    data object Inbox : Route
    data object People : Route
    data class Onboarding(val step: Int = 0) : Route
}

/** All state comes from the vault. The UI only reads these lists and calls the actions. */
class AppState(app: Application) : AndroidViewModel(app) {
    val store = VaultStore((app.getExternalFilesDir(null) ?: app.filesDir).resolve("vault"))
    var promises by mutableStateOf(listOf<Promise>()); private set
    var people by mutableStateOf(listOf<Pair<String, Int>>()); private set
    var notes by mutableStateOf(listOf<app.nayom.data.Note>()); private set
    var day by mutableStateOf(Day(0, 0, 0, 0, 0)); private set
    var debriefed by mutableStateOf(setOf<String>()); private set
    var now by mutableStateOf(ZonedDateTime.now()); private set
    private var routeState by mutableStateOf<Route>(Route.Home)
    var route: Route
        get() = routeState
        set(v) { routeState = v; store.log("open", v::class.simpleName.orEmpty()) }
    var tab by mutableStateOf(0)
    var permTick by mutableStateOf(0)
    val calendar = CalendarBridge(app)
    var busy by mutableStateOf(0.0); private set
    private val flag get() = java.io.File(store.root, "Nayom/.onboarded")
    var onboarded by mutableStateOf(java.io.File(store.root, "Nayom/.onboarded").exists()); private set
    fun finishOnboarding() { flag.parentFile?.mkdirs(); flag.writeText("1"); onboarded = true; store.log("onboarded"); route = Route.Home }
    fun replayIntro() { route = Route.Onboarding(0) }
    var activity by mutableStateOf(listOf<VaultStore.Entry>()); private set
    fun loadActivity() { activity = store.activity() }

    init {
        refresh()
        viewModelScope.launch { while (true) { delay(1000); now = ZonedDateTime.now(); if (now.second == 0) { store.sweepMissed(); refresh() } } }
    }

    fun refresh() {
        store.sweepMissed()
        promises = store.promises(); people = store.people(); notes = store.notes()
        day = store.today(); debriefed = store.debriefed()
        busy = runCatching { calendar.busyHours(ZonedDateTime.now()) }.getOrDefault(0.0)
    }
    val open get() = promises.filter { it.status == Status.Open }
    val next get() = open.firstOrNull()
    val dueSoon get() = open.count { it.due.isBefore(now.plusHours(48)) }
    val unhandledMiss get() = promises.lastOrNull { it.status == Status.Missed && it.id !in debriefed }

    var chat by mutableStateOf(listOf<VaultStore.Chat>()); private set
    var chatDraft by mutableStateOf<String?>(null); var chatDone by mutableStateOf<String?>(null)
    fun loadChat() { chat = store.chatToday(); if (chat.isEmpty()) { store.chat(false, "Come, sit. Did you eat? What is pending today? Tell me."); chat = store.chatToday() } }
    fun say(text: String) {
        store.chat(true, text)
        val r = MomChat.reply(text, open, wasterMin(), ZonedDateTime.now())
        store.chat(false, r.text); chatDraft = r.draft; chatDone = r.markDoneId; chat = store.chatToday()
        runCatching { Brain(getApplication()).event("chat", null, text) }
    }
    private fun wasterMin() = runCatching { Habits(getApplication()).wasterToday() }.getOrDefault(0)

    fun back() { route = Route.Home }
    fun addPromise(who: String, what: String, due: ZonedDateTime, app: String = "nayom") {
        store.addPromise(who, what, due, app)
        runCatching { calendar.add("$what ($who)", due)?.let { store.log("calendar", "$what -> event $it") } }
        refresh(); back()
    }
    /** Opens the share sheet inside the app where the promise was made, so the update goes to the same person. */
    fun tellThem(p: Promise, text: String) {
        val ctx = getApplication<Application>()
        val send = android.content.Intent(android.content.Intent.ACTION_SEND).setType("text/plain").putExtra(android.content.Intent.EXTRA_TEXT, text)
        val tried = if (p.app != "nayom" && p.app.contains('.')) runCatching { ctx.startActivity(android.content.Intent(send).setPackage(p.app).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)); true }.getOrDefault(false) else false
        if (!tried) ctx.startActivity(android.content.Intent.createChooser(send, "Send to ${p.person}").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK))
        store.log("update-sent", "${p.id} | ${p.person} | ${p.app}")
    }
    fun markStay(id: String, s: Status, newDue: ZonedDateTime? = null) { store.setStatus(id, s, newDue); if (s == Status.Kept) { app.nayom.service.OverlayService.mood("happy", 6000); app.nayom.service.OverlayService.say(app.nayom.data.Mom.kept(), 5000) }; refresh() }
    fun mark(id: String, s: Status, newDue: ZonedDateTime? = null) { store.setStatus(id, s, newDue); if (s == Status.Kept) app.nayom.service.OverlayService.mood("happy", 6000); app.nayom.service.OverlayService.say(app.nayom.data.Mom.kept(), 5000); refresh(); back() }
    fun ask(from: String, what: String): String { val a = store.addAsk(from, what); refresh(); return a.id }
    fun decide(id: String, d: Decision) { store.decide(id, d); refresh() }
    fun debrief(id: String, reason: String) { store.addDebrief(id, reason); refresh(); back() }
    fun saveNote(title: String, body: String) { store.addNote(title, body); refresh(); back() }
}
