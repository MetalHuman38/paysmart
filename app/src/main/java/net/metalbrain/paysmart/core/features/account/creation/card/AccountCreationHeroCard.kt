package net.metalbrain.paysmart.core.features.account.creation.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

/**
 * A hero card component used within the account creation flow to highlight specific steps or sections.
 * Displays an optional emoji, a prominent title, and a descriptive subtitle over a themed gradient background.
 *
 * @param modifier The [Modifier] to be applied to the card.
 * @param emoji An optional emoji string to be displayed at the top of the card.
 * @param title The main headline text for the card.
 * @param subtitle The supporting text providing additional details.
 */
@Composable
internal fun AccountCreationHeroCard(
    modifier: Modifier = Modifier,
    emoji: String? = null,
    title: String,
    subtitle: String,
) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens


    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfacePrimary,
            contentColor = colors.textPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colors.brandPrimary.copy(alpha = 0.12f),
                            colors.brandAccent.copy(alpha = 0.08f),
                            colors.surfacePrimary
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = Dimens.lg, vertical = Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.lg)
            ) {
                emoji
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        Text(
                            text = it,
                            style = typography.heading1
                        )
                    }

                Text(
                    text = title,
                    style = typography.heading3,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
