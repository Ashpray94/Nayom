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

@Composable
private fun Screen(content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)

/** S-01 Today: one focal point — the promise that is closest to due. */
@Composable
fun TodayScreen() = Screen {
    Label("Today")
    Text("Naya Mom is watching the clock.", style = NayomType.display, color = colors.ink)
    Card {
        Label("Next promise")
        Spacer(Modifier.height(8.dp))
        Text("02:50:12", style = NayomType.numeral, color = colors.ink)
        Text("Final banner file · due 17:00", style = NayomType.body, color = colors.muted)
    }
    Card {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Tile(R.drawable.ic_promises, colors.teal)
            Column { Text("3 promises due in 48 h", style = NayomType.body, color = colors.ink); Label("9 of 11 this week") }
        }
    }
}

/** I-01 Gate card: shown over the app when you are about to say yes. Yes and Later unlock after the pause. */
@Composable
fun GateScreen() = Screen {
    Label("Before you say yes")
    Text("Ravi asks: Banner tweak, today.", style = NayomType.heading, color = colors.ink)
    Card {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Tile(R.drawable.ic_clock, colors.amber)
            Text("Pushes out Meena's file", style = NayomType.body, color = colors.ink)
        }
    }
    Button("No, kindly", primary = true, modifier = Modifier.fillMaxWidth()) {}
    Button("Later", primary = false, modifier = Modifier.fillMaxWidth()) {}
    Button("Yes", primary = false, modifier = Modifier.fillMaxWidth()) {}
    Label("Yes and Later unlock in 8 s")
}

/** I-02 Slip card: firm, never shaming. */
@Composable
fun SlipScreen() = Screen {
    Label("Missed")
    Text("Meena's final banner file was due at 17:00.", style = NayomType.heading, color = colors.ink)
    Card {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Tile(R.drawable.ic_missed, colors.berry)
            Text("No file saved or sent since 09:15.", style = NayomType.body, color = colors.ink)
        }
    }
    Button("Debrief", primary = true, modifier = Modifier.fillMaxWidth()) {}
    Button("Reschedule", primary = false, modifier = Modifier.fillMaxWidth()) {}
}

/** S-08 Evening sheet. */
@Composable
fun EveningScreen() = Screen {
    Label("Evening")
    Text("Phone down.", style = NayomType.hero, color = colors.ink)
    Card {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Missed" to colors.berry, "Moved" to colors.ink, "Reflex yes" to colors.ink).forEach { (k, c) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("1", style = NayomType.numeral, color = c); Label(k)
                }
            }
        }
    }
    Button("Talk to Naya Mom", primary = true, modifier = Modifier.fillMaxWidth()) {}
}

/** S-17 Setup: what Nayom may see, and the things she never reads. */
@Composable
fun SetupScreen() = Screen {
    Label("Setup")
    Text("What I may see", style = NayomType.heading, color = colors.ink)
    Card {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Tile(R.drawable.ic_check, colors.teal)
            Text("Lets me see what you type and what is on screen.", style = NayomType.body, color = colors.ink)
        }
    }
    Card {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Tile(R.drawable.ic_shield, colors.sky)
            Text("Never: banking, OTP, authenticator and password apps.", style = NayomType.body, color = colors.ink)
        }
    }
    Button("Allow", primary = true, modifier = Modifier.fillMaxWidth()) {}
}
