package app.nayom.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.nayom.R
import app.nayom.ui.components.*
import app.nayom.ui.theme.*

/** Every screen: 24 edge, 24 between ideas, one focal point, actions pinned at the bottom. */
@Composable
private fun Screen(actions: (@Composable ColumnScope.() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = Space.edge.dp).padding(top = 16.dp, bottom = 24.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Space.gap.dp), content = content)
        if (actions != null) Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = actions)
    }

@Composable
private fun IconLine(icon: Int, tint: androidx.compose.ui.graphics.Color, text: String) =
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Tile(icon, tint); Text(text, style = NayomType.body, color = colors.ink, modifier = Modifier.weight(1f))
    }

/** S-01 Today: one focal point, the promise closest to due. No serif. */
@Composable
fun TodayScreen() = Screen {
    Label("Today")
    Card {
        Label("Next promise")
        Spacer(Modifier.height(16.dp))
        Text("02:50:12", style = NayomType.numeral, color = colors.ink)
        Spacer(Modifier.height(8.dp))
        Text("Final banner file", style = NayomType.title, color = colors.ink)
        Text("Due 17:00", style = NayomType.body, color = colors.muted)
    }
    Card { IconLine(R.drawable.ic_promises, colors.teal, "3 promises due in 48 h") }
}

/** I-01 Gate card. Yes and Later unlock after the pause. */
@Composable
fun GateScreen() = Screen(actions = {
    Button("No, kindly", true, Modifier.fillMaxWidth()) {}
    Button("Later", false, Modifier.fillMaxWidth()) {}
    Button("Yes", false, Modifier.fillMaxWidth()) {}
    Spacer(Modifier.height(4.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) { Label("Yes and Later unlock in 8 s") }
}) {
    Label("Before you say yes")
    Text("Ravi asks: Banner tweak, today.", style = NayomType.title, color = colors.ink)
    Card { IconLine(R.drawable.ic_clock, colors.amber, "Pushes out Meena's file") }
}

/** I-02 Slip card: firm, never shaming. */
@Composable
fun SlipScreen() = Screen(actions = {
    Button("Debrief", true, Modifier.fillMaxWidth()) {}
    Button("Reschedule", false, Modifier.fillMaxWidth()) {}
}) {
    Text("MISSED", style = NayomType.status, color = colors.berry)
    Text("Meena's final banner file was due at 17:00.", style = NayomType.title, color = colors.ink)
    Card { IconLine(R.drawable.ic_missed, colors.berry, "No file saved or sent since 09:15.") }
}

/** S-08 Evening. The one serif moment: a short closing line. */
@Composable
fun EveningScreen() = Screen(actions = {
    Button("Talk to Naya Mom", true, Modifier.fillMaxWidth()) {}
}) {
    Label("Evening")
    Text("Phone down.", style = NayomType.hero, color = colors.ink)
    Card {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Missed" to colors.berry, "Moved" to colors.ink, "Reflex yes" to colors.ink).forEach { (k, c) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1", style = NayomType.numeral, color = c)
                    Spacer(Modifier.height(8.dp)); Label(k)
                }
            }
        }
    }
}

/** S-17 Setup. */
@Composable
fun SetupScreen() = Screen(actions = {
    Button("Allow", true, Modifier.fillMaxWidth()) {}
}) {
    Label("Setup")
    Text("What I may see", style = NayomType.title, color = colors.ink)
    Card { IconLine(R.drawable.ic_check, colors.teal, "What you type and what is on screen.") }
    Card { IconLine(R.drawable.ic_shield, colors.sky, "Never: banking, OTP, authenticator and password apps.") }
}
