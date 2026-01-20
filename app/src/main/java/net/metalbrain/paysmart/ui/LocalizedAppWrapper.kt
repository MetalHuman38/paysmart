package net.metalbrain.paysmart.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.core.locale.LocaleManager
import net.metalbrain.paysmart.ui.viewmodel.LanguageViewModel

@Composable
fun LocalizedAppWrapper(
    content: @Composable () -> Unit
) {
    val viewModel: LanguageViewModel = hiltViewModel()


    val languageCode by viewModel.currentLanguage.collectAsState()

    val context = LocalContext.current

    // Get the localized context, which may not be an Activity
    val localizedContext = remember(languageCode) {
        LocaleManager.wrapContext(context, languageCode)
    }

    // Find the original Activity from the context hierarchy
    val activity = context.findActivity()

    // Create a new ContextWrapper that holds the Activity but uses the localized resources.
    // This ensures Hilt can find the Activity while the UI uses the correct locale.
    val hiltFriendlyContext = remember(localizedContext, activity) {
        if (activity != null) {
            object : ContextWrapper(activity) {
                override fun getResources() = localizedContext.resources
                override fun getTheme() = localizedContext.theme
            }
        } else {
            localizedContext
        }
    }

    CompositionLocalProvider(
        LocalContext provides hiltFriendlyContext,
        LocalLayoutDirection provides if (LocaleManager.isRtl(languageCode)) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        content()
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
