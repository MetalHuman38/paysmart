package net.metalbrain.paysmart.ui.screens.startup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.ui.components.LanguageSelector
import net.metalbrain.paysmart.ui.screens.PaySmartTopBarRow

@Composable
fun StartupTopBar(
    currentLanguage: String,
    onLanguageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PaySmartTopBarRow(
        modifier = modifier,
        endContent = {
            LanguageSelector(
                currentLanguage = currentLanguage,
                onClick = onLanguageClick
            )
        }
    )
}
