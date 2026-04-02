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
import java.util.Locale

@Composable
fun ServiceItemCard(service: HomeServiceTile) {
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
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private data class ServiceVisualStyle(
    val containerColor: Color,
    val contentColor: Color
)

@Composable
private fun serviceVisualStyle(service: HomeServiceTile): ServiceVisualStyle {
    val normalized = service.id.trim().uppercase(Locale.US)
    return when {
        normalized.contains("SEND") -> ServiceVisualStyle(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.56f),
            contentColor = MaterialTheme.colorScheme.primary
        )

        normalized.contains("RECEIVE") -> ServiceVisualStyle(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.56f),
            contentColor = MaterialTheme.colorScheme.secondary
        )

        normalized.contains("INVOICE") -> ServiceVisualStyle(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.52f),
            contentColor = MaterialTheme.colorScheme.tertiary
        )

        else -> ServiceVisualStyle(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}
