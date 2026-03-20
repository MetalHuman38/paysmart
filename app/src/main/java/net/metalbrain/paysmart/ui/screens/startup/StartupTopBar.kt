package net.metalbrain.paysmart.ui.screens.startup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.ui.components.LanguageSelector
import net.metalbrain.paysmart.ui.theme.Dimens


@Composable
internal fun StartupTopBar(
    currentLanguage: String,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Dimens.mediumSpacing, bottom = Dimens.largeSpacing + Dimens.md),
        horizontalArrangement = Arrangement.End
    ) {
        LanguageSelector(
            currentLanguage = currentLanguage,
            onClick = onLanguageClick
        )
    }
}
