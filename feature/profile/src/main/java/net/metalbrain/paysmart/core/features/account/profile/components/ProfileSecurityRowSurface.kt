package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack


@Composable
internal fun ProfileSecurityRowSurface(
    onClick: (() -> Unit)?,
    accentColor: Color,
    content: @Composable RowScope.() -> Unit
) {
    val securityStyle = LocalAppThemePack.current.securityStyle
    val editorialLayout = securityStyle.useEditorialLayout
    val baseColor = if (editorialLayout) {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = securityStyle.glassPanelAlpha)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(if (editorialLayout) 24.dp else 18.dp),
        color = baseColor,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(
                alpha = securityStyle.ghostBorderAlpha + if (editorialLayout) 0.08f else 0.04f
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.md, vertical = Dimens.md),
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}
