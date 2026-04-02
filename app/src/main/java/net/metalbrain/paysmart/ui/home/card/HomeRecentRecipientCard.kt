package net.metalbrain.paysmart.ui.home.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.capabilities.catalog.CurrencyFlagResolver
import net.metalbrain.paysmart.ui.home.state.HomeRecentRecipient
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.HomeCardTokens
import java.util.Locale

@Composable
fun HomeRecentRecipientCard(
    recipient: HomeRecentRecipient,
    preferredCurrencyCode: String,
    preferredFlagEmoji: String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val initials = recipientInitials(recipient.displayName)
    val flagEmoji = CurrencyFlagResolver.resolve(
        context = context,
        currencyCode = recipient.targetCurrencyCode,
        preferredCurrencyCode = preferredCurrencyCode,
        preferredFlagEmoji = preferredFlagEmoji
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(HomeCardTokens.recentRecipientCardWidth)
            .size(
                width = HomeCardTokens.recentRecipientCardWidth,
                height = HomeCardTokens.recentRecipientCardHeight
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = HomeCardTokens.subtleElevation)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
                Box(
                    modifier = Modifier.size(HomeCardTokens.recentRecipientAvatarSize),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.matchParentSize(),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                        )
                    }

                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(26.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = flagEmoji,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Text(
                    text = recipient.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = recipient.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun recipientInitials(displayName: String): String {
    val parts = displayName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    if (parts.isEmpty()) {
        return "?"
    }

    return when {
        parts.size == 1 -> parts.first().take(2).uppercase(Locale.US)
        else -> "${parts.first().first()}${parts.last().first()}".uppercase(Locale.US)
    }
}
