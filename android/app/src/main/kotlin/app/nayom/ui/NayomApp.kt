package app.nayom.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.nayom.R
import app.nayom.ui.components.Icon
import app.nayom.ui.screens.*
import app.nayom.ui.theme.*

private enum class Tab(val label: String, val icon: Int, val active: Int) {
    Today("Today", R.drawable.ic_today, R.drawable.ic_today_active),
    Promises("Promises", R.drawable.ic_promises, R.drawable.ic_promises_active),
    People("People", R.drawable.ic_people, R.drawable.ic_people_active),
    Notes("Notes", R.drawable.ic_notes, R.drawable.ic_notes_active),
}

@Composable
fun NayomApp(st: AppState = viewModel()) {
    BackHandler(enabled = st.route != Route.Home) { st.back() }
    Column(Modifier.fillMaxSize().background(colors.bg)) {
        Box(Modifier.weight(1f)) {
            when (val r = st.route) {
                Route.Home -> when (Tab.entries[st.tab]) {
                    Tab.Today -> TodayScreen(st); Tab.Promises -> PromisesScreen(st)
                    Tab.People -> PeopleScreen(st); Tab.Notes -> NotesScreen(st)
                }
                is Route.PromiseDetail -> PromiseDetailScreen(st, r.id)
                is Route.NewPromise -> NewPromiseScreen(st, r)
                Route.AskIn -> AskInScreen(st)
                is Route.Gate -> GateScreen(st, r.askId)
                is Route.Debrief -> DebriefScreen(st, r.id)
                is Route.Note -> NoteScreen(st, r)
                Route.Evening -> EveningScreen(st)
                Route.Setup -> SetupScreen(st)
            }
        }
        if (st.route == Route.Home) Row(Modifier.fillMaxWidth().background(colors.surface).navigationBarsPadding().padding(top = 12.dp, bottom = 8.dp)) {
            Tab.entries.forEachIndexed { i, t ->
                val on = i == st.tab
                Column(Modifier.weight(1f).clickable { st.tab = i }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (on) t.active else t.icon, if (on) colors.teal else colors.muted)
                    Spacer(Modifier.height(6.dp))
                    Text(t.label.uppercase(), style = NayomType.label, color = if (on) colors.teal else colors.muted)
                }
            }
        } else Spacer(Modifier.navigationBarsPadding())
    }
}
