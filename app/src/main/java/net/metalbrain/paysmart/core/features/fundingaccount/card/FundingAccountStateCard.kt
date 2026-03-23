package net.metalbrain.paysmart.core.features.fundingaccount.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountScreenPhase
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountUiState
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
internal fun FundingAccountStateCard(
    state: FundingAccountUiState,
    onRefresh: () -> Unit,
    onProvision: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = resolveFundingAccountVisual(state)

    FundingAccountSurfaceCard(
        modifier = modifier,
        accentColor = visual.accentColor,
        highlighted = state.phase in setOf(
            FundingAccountScreenPhase.READY,
            FundingAccountScreenPhase.PENDING
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.md),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = visual.accentColor.copy(alpha = 0.14f)
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = visual.contentDescription,
                        tint = visual.accentColor
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                Text(
                    text = visual.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = visual.supporting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        when {
            state.canProvision -> {
                PrimaryButton(
                    text = if (state.account == null) {
                        stringResource(R.string.funding_account_action_provision)
                    } else {
                        stringResource(R.string.funding_account_action_retry)
                    },
                    onClick = onProvision,
                    isLoading = state.isProvisioning
                )
            }

            state.phase == FundingAccountScreenPhase.ERROR && state.account == null -> {
                PrimaryButton(
                    text = stringResource(R.string.funding_account_action_refresh),
                    onClick = onRefresh,
                    isLoading = state.isRefreshing
                )
            }
        }
    }
}

@Composable
private fun resolveFundingAccountVisual(state: FundingAccountUiState): FundingAccountStateVisual {
    val colorScheme = MaterialTheme.colorScheme
    return when (state.phase) {
        FundingAccountScreenPhase.READY -> FundingAccountStateVisual(
            icon = Icons.Filled.CheckCircle,
            title = stringResource(R.string.funding_account_state_ready_title),
            supporting = stringResource(R.string.funding_account_state_ready_supporting),
            contentDescription = stringResource(R.string.funding_account_status_ready_content_description),
            accentColor = colorScheme.tertiary
        )

        FundingAccountScreenPhase.PENDING -> FundingAccountStateVisual(
            icon = Icons.Filled.Schedule,
            title = stringResource(R.string.funding_account_state_pending_title),
            supporting = stringResource(R.string.funding_account_state_pending_supporting),
            contentDescription = stringResource(R.string.funding_account_status_pending_content_description),
            accentColor = colorScheme.primary
        )

        FundingAccountScreenPhase.EMPTY -> FundingAccountStateVisual(
            icon = Icons.Filled.AccountBalance,
            title = stringResource(R.string.funding_account_state_empty_title),
            supporting = stringResource(R.string.funding_account_state_empty_supporting),
            contentDescription = stringResource(R.string.funding_account_status_loading_content_description),
            accentColor = colorScheme.primary
        )

        FundingAccountScreenPhase.KYC_REQUIRED -> FundingAccountStateVisual(
            icon = Icons.Filled.Verified,
            title = stringResource(R.string.funding_account_state_kyc_required_title),
            supporting = stringResource(R.string.funding_account_state_kyc_required_supporting),
            contentDescription = stringResource(R.string.funding_account_status_error_content_description),
            accentColor = colorScheme.secondary
        )

        FundingAccountScreenPhase.UNSUPPORTED_MARKET -> FundingAccountStateVisual(
            icon = Icons.Filled.ErrorOutline,
            title = stringResource(R.string.funding_account_state_unsupported_title),
            supporting = stringResource(
                R.string.funding_account_state_unsupported_supporting_format,
                state.countryName
            ),
            contentDescription = stringResource(R.string.funding_account_status_error_content_description),
            accentColor = colorScheme.secondary
        )

        FundingAccountScreenPhase.ERROR -> FundingAccountStateVisual(
            icon = Icons.Filled.ErrorOutline,
            title = stringResource(R.string.funding_account_state_error_title),
            supporting = stringResource(R.string.funding_account_state_error_supporting),
            contentDescription = stringResource(R.string.funding_account_status_error_content_description),
            accentColor = colorScheme.error
        )

        FundingAccountScreenPhase.LOADING -> FundingAccountStateVisual(
            icon = Icons.Filled.Schedule,
            title = stringResource(R.string.funding_account_state_loading_title),
            supporting = stringResource(R.string.funding_account_state_loading_supporting),
            contentDescription = stringResource(R.string.funding_account_status_loading_content_description),
            accentColor = colorScheme.primary
        )
    }
}

private data class FundingAccountStateVisual(
    val icon: ImageVector,
    val title: String,
    val supporting: String,
    val contentDescription: String,
    val accentColor: Color
)
