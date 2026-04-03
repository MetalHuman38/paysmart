package net.metalbrain.paysmart.core.features.account.passkey.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun PasskeySurfaceCard(
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    highlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = Dimens.lg, vertical = Dimens.lg),
    content: @Composable ColumnScope.() -> Unit
) {
    val securityStyle = LocalAppThemePack.current.securityStyle
    val editorialLayout = securityStyle.useEditorialLayout
    val baseColor = if (highlighted) {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = if (editorialLayout) 0.92f else 0.98f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (editorialLayout) 0.88f else 0.96f)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (editorialLayout) 30.dp else 24.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(
                alpha = if (editorialLayout) {
                    securityStyle.ghostBorderAlpha + 0.08f
                } else {
                    0.16f
                }
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = if (highlighted) 0.18f else 0.10f),
                            Color.Transparent,
                            baseColor
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
                content = content
            )
        }
    }
}
