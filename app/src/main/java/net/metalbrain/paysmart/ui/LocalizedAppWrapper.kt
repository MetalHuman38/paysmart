import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import net.metalbrain.paysmart.ui.viewmodel.LanguageViewModel
import net.metalbrain.paysmart.utils.LocaleUtils

@Composable
fun LocalizedAppWrapper(
    languageViewModel: LanguageViewModel,
    content: @Composable () -> Unit
) {
    val langCode = languageViewModel.currentLanguage.collectAsState().value
    val baseContext = LocalContext.current
    val localizedContext = remember(langCode) {
        LocaleUtils.applyLanguage(baseContext, langCode)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        content()
    }
}
