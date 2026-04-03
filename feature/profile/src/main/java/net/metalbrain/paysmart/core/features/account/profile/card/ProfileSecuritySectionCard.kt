package net.metalbrain.paysmart.core.features.account.profile.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun ProfileSecuritySectionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    val securityStyle = LocalAppThemePack.current.securityStyle
    val editorialLayout = securityStyle.useEditorialLayout
    val containerColor = if (editorialLayout) {
        MaterialTheme.colorScheme.surfaceContainer.copy(alpha = securityStyle.glassPanelAlpha)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (editorialLayout) 30.dp else 24.dp),
        color = containerColor,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = securityStyle.ghostBorderAlpha + 0.06f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.lg, vertical = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                subtitle?.takeIf { it.isNotBlank() }?.let { supporting ->
                    Text(
                        text = supporting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(if (editorialLayout) 24.dp else 20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (editorialLayout) 0.72f else 0.84f),
                border = BorderStroke(
                    1.dp,
                    accentColor.copy(alpha = if (editorialLayout) 0.16f else 0.10f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.sm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                    content = content
                )
            }
        }
    }
}
