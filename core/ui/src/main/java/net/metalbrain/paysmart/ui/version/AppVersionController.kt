package net.metalbrain.paysmart.ui.version

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import net.metalbrain.paysmart.core.common.runtime.AppVersionInfo

val LocalAppVersionInfo = staticCompositionLocalOf<AppVersionInfo> {
    error("LocalAppVersionInfo not provided")
}

@Composable
fun ProvideAppVersionInfo(
    appVersionInfo: AppVersionInfo,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppVersionInfo provides appVersionInfo,
        content = content,
    )
}
