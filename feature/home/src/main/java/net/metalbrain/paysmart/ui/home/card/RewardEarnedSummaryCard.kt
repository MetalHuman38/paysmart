package net.metalbrain.paysmart.ui.home.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import net.metalbrain.paysmart.feature.home.R
import net.metalbrain.paysmart.ui.home.state.RewardEarnedSnapshot
import net.metalbrain.paysmart.ui.theme.HomeCardTokens
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme
import java.util.Locale

@Composable
fun RewardEarnedSummaryCard(
    isBalanceVisible: Boolean,
    snapshot: RewardEarnedSnapshot,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val colors = PaysmartTheme.colorTokens
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(HomeCardTokens.summaryCardHeight),
        shape = HomeCardTokens.cardShape,
        colors = CardDefaults.cardColors(containerColor = colors.surfacePrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = HomeCardTokens.defaultElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            colors.surfaceElevated,
                            colors.brandAccent.copy(alpha = 0.18f),
                            colors.brandPrimary.copy(alpha = 0.24f)
                        )
                    )
                )
                .padding(HomeCardTokens.contentPadding),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                Icon(
                    imageVector = Icons.Outlined.WorkspacePremium,
                    contentDescription = null,
                    tint = colors.brandAccent
                )

                Text(
                    text = maskedValue(
                        isBalanceVisible,
                        "${formatAmount(snapshot.points)} pts"
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = stringResource(id = R.string.earn_more_by_completing_transfers_and_invites),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%.2f", amount)
}

