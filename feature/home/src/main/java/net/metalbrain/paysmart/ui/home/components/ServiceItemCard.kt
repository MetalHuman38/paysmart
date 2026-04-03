package net.metalbrain.paysmart.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.HomeCardTokens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import java.util.Locale

@Composable
fun ServiceItemCard(service: HomeServiceTile) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens
    val visual = serviceVisualStyle(service)

    Column(
        modifier = Modifier
            .width(HomeCardTokens.serviceCardWidth)
            .clickable(onClick = service.onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        Surface(
            modifier = Modifier.size(HomeCardTokens.serviceCircleSize),
            shape = MaterialTheme.shapes.extraLarge,
            color = visual.containerColor,
            tonalElevation = HomeCardTokens.subtleElevation
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = service.icon,
                    contentDescription = service.title,
                    tint = visual.contentColor
                )
            }
        }

        Text(
            text = service.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
    }
}

private data class ServiceVisualStyle(
    val containerColor: Color,
    val contentColor: Color
)

@Composable
private fun serviceVisualStyle(service: HomeServiceTile): ServiceVisualStyle {
    val colors = PaysmartTheme.colorTokens
    val normalized = service.id.trim().uppercase(Locale.US)
    return when {
        normalized.contains("SEND") -> ServiceVisualStyle(
            containerColor = colors.brandPrimary.copy(alpha = 0.16f),
            contentColor = colors.brandPrimary
        )

        normalized.contains("RECEIVE") -> ServiceVisualStyle(
            containerColor = colors.info.copy(alpha = 0.14f),
            contentColor = colors.info
        )

        normalized.contains("INVOICE") -> ServiceVisualStyle(
            containerColor = colors.success.copy(alpha = 0.16f),
            contentColor = colors.success
        )

        else -> ServiceVisualStyle(
            containerColor = colors.surfaceElevated,
            contentColor = colors.textPrimary
        )
    }
}
