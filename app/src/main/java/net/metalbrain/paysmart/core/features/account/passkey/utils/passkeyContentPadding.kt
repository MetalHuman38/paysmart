package net.metalbrain.paysmart.core.features.account.passkey.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun passkeyContentPadding(): PaddingValues {
    val securityStyle = LocalAppThemePack.current.securityStyle
    val horizontal = if (securityStyle.useEditorialLayout) {
        securityStyle.outerHorizontalPadding
    } else {
        Dimens.screenPadding
    }
    return PaddingValues(
        start = horizontal,
        top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + Dimens.md,
        end = horizontal,
        bottom = Dimens.xl
    )
}
