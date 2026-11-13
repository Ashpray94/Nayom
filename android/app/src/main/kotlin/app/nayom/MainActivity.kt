package app.nayom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.nayom.ui.NayomApp
import app.nayom.ui.theme.NayomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NayomTheme { NayomApp() } }
    }
}
