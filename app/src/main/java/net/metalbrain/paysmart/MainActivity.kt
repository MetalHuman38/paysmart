package net.metalbrain.paysmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import net.metalbrain.paysmart.ui.AppNavGraph
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import net.metalbrain.paysmart.ui.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.utils.LocaleUtils

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LanguageViewModel = hiltViewModel()
            val langCode by viewModel.currentLanguage.collectAsState()

            val baseContext = LocalContext.current
            val localizedContext = remember(langCode) {
                LocaleUtils.applyLanguage(baseContext, langCode)
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                PaysmartTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        AppNavGraph(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
