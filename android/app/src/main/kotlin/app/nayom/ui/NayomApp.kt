package app.nayom.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

/** Shell with the 4-tab bar. Gate/Slip/Evening/Setup are previewed here until the services trigger them. */
@Composable
fun NayomApp() {
    var tab by remember { mutableStateOf(Tab.Today) }
    Column(Modifier.fillMaxSize().background(colors.bg)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                Tab.Today -> TodayScreen()
                Tab.Promises -> SlipScreen()
                Tab.People -> GateScreen()
                Tab.Notes -> EveningScreen()
            }
        }
        Row(Modifier.fillMaxWidth().background(colors.surface).navigationBarsPadding().padding(top = 12.dp, bottom = 8.dp)) {
            Tab.values().forEach { t ->
                val on = t == tab
                Column(Modifier.weight(1f).clickable { tab = t }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(if (on) t.active else t.icon, if (on) colors.teal else colors.muted)
                    Spacer(Modifier.height(6.dp))
                    Text(t.label.uppercase(), style = NayomType.label, color = if (on) colors.teal else colors.muted)
                }
            }
        }
    }
}
