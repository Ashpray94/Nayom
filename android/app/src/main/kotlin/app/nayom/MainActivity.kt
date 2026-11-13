package app.nayom

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import app.nayom.data.Prefs
import app.nayom.service.OverlayService
import app.nayom.ui.AppState
import app.nayom.ui.NayomApp
import app.nayom.ui.Route
import app.nayom.ui.theme.NayomTheme

class MainActivity : ComponentActivity() {
    private val st: AppState by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { NayomTheme { NayomApp(st) } }
        handle(intent)
    }

    override fun onNewIntent(intent: Intent) { super.onNewIntent(intent); handle(intent) }

    private fun handle(i: Intent?) {
        if (i?.getStringExtra("route") == "inbox") { if (st.onboarded) st.route = Route.Inbox; i.removeExtra("route"); return }
        i?.getStringExtra("checkin")?.let { if (st.onboarded) st.route = Route.CheckIn(it); i.removeExtra("checkin"); return }
        val what = i?.getStringExtra("what") ?: return
        if (st.onboarded) st.route = Route.NewPromise(i.getStringExtra("who").orEmpty(), what, i.getIntExtra("due", 1), i.getStringExtra("app").orEmpty())
        i.removeExtra("what")
    }

    override fun onResume() {
        super.onResume()
        st.permTick++
        if (Settings.canDrawOverlays(this) && Prefs(this).avatarOn) OverlayService.start(this)
    }
}
