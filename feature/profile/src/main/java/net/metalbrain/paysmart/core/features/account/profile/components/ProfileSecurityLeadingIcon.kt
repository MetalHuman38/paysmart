package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
internal fun ProfileSecurityLeadingIcon(
    icon: ImageVector,
    accentColor: Color
) {
    val securityStyle = LocalAppThemePack.current.securityStyle
    val editorialLayout = securityStyle.useEditorialLayout

    Surface(
        shape = RoundedCornerShape(if (editorialLayout) 18.dp else 14.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (editorialLayout) 0.42f else 0.9f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = securityStyle.ghostBorderAlpha + 0.06f)
        )
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor
            )
        }
    }
}
