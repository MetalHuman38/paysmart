package net.metalbrain.paysmart

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import dagger.hilt.android.AndroidEntryPoint
import net.metalbrain.paysmart.ui.AppNavGraph
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import net.metalbrain.paysmart.utils.LocaleUtils

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(base: Context) {
        val langCode = base.getSharedPreferences("app_settings", MODE_PRIVATE)
            .getString("selected_language", "en") ?: "en"
        val localizedContext = LocaleUtils.setAppLocale(base, langCode)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaysmartTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavGraph()
                }
            }
        }
    }
}
