package net.metalbrain.paysmart.core.features.account.passkey.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

enum class PasskeyStatusTone {
    Neutral,
    Active,
    Danger
}

@Composable
fun PasskeyScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val securityStyle = LocalAppThemePack.current.securityStyle
    val editorialLayout = securityStyle.useEditorialLayout
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(
                colors = if (editorialLayout) {
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceColorAtElevation(Dimens.md),
                        MaterialTheme.colorScheme.background
                    )
                } else {
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.16f),
                        MaterialTheme.colorScheme.background
                    )
                }
            )
        ),
        content = content
    )
}
