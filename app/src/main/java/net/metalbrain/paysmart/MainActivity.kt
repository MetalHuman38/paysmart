package net.metalbrain.paysmart

import android.app.ComponentCaller
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import net.metalbrain.paysmart.core.locale.LocaleManager
import net.metalbrain.paysmart.ui.AppNavGraph
import net.metalbrain.paysmart.ui.LocalizedAppWrapper
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.applyLocale(base))
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PaysmartTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    LocalizedAppWrapper {
                        AppNavGraph()
                    }
                }
            }
        }
    }
}
