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
    data class NewPromise(val who: String = "", val what: String = "") : Route
    data object AskIn : Route
    data class Gate(val askId: String) : Route
    data class Debrief(val id: String) : Route
    data class Note(val file: java.io.File?) : Route
    data object Evening : Route
    data object Setup : Route
    data object Activity : Route
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
    }
    val open get() = promises.filter { it.status == Status.Open }
    val next get() = open.firstOrNull()
    val dueSoon get() = open.count { it.due.isBefore(now.plusHours(48)) }
    val unhandledMiss get() = promises.lastOrNull { it.status == Status.Missed && it.id !in debriefed }

    fun back() { route = Route.Home }
    fun addPromise(who: String, what: String, due: ZonedDateTime) { store.addPromise(who, what, due); refresh(); back() }
    fun mark(id: String, s: Status, newDue: ZonedDateTime? = null) { store.setStatus(id, s, newDue); refresh(); back() }
    fun ask(from: String, what: String): String { val a = store.addAsk(from, what); refresh(); return a.id }
    fun decide(id: String, d: Decision) { store.decide(id, d); refresh() }
    fun debrief(id: String, reason: String) { store.addDebrief(id, reason); refresh(); back() }
    fun saveNote(title: String, body: String) { store.addNote(title, body); refresh(); back() }
}
