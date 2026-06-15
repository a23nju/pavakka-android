package ai.laennec.pavakka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ai.laennec.pavakka.core.ui.theme.PavakkaTheme
import ai.laennec.pavakka.core.ui.RootScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PavakkaTheme {
                RootScreen()
            }
        }
    }
}
