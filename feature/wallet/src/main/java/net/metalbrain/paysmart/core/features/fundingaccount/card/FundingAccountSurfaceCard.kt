package net.metalbrain.paysmart.core.features.fundingaccount.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
internal fun FundingAccountSurfaceCard(
    accentColor: Color,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(Dimens.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    val themePack = LocalAppThemePack.current
    val colorScheme = MaterialTheme.colorScheme
    val borderAlpha = themePack.buttonStyle.ghostBorderAlpha.coerceAtLeast(0.12f)
    val containerColor = if (highlighted) {
        colorScheme.surfaceContainer
    } else {
        colorScheme.surfaceContainerLow
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = containerColor.copy(alpha = 0.96f),
        tonalElevation = if (highlighted) 10.dp else 4.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = 1.dp,
            color = colorScheme.outline.copy(alpha = borderAlpha)
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (highlighted) 0.16f else 0.08f),
                            Color.Transparent,
                            colorScheme.surfaceContainerHighest.copy(alpha = 0.18f)
                        ),
                        start = Offset.Zero,
                        end = Offset(720f, 720f)
                    )
                )
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
            content = content
        )
    }
}
