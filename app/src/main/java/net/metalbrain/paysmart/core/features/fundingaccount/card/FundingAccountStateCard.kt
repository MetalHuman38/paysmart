package net.metalbrain.paysmart.core.features.fundingaccount.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = visual.containerColor,
            contentColor = visual.contentColor
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(Dimens.minimumTouchTarget),
                    shape = MaterialTheme.shapes.medium,
                    color = visual.iconContainerColor
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = visual.contentDescription,
                        modifier = Modifier
                            .padding(Dimens.sm)
                            .size(Dimens.lg),
                        tint = visual.iconTint
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                ) {
                    Text(
                        text = visual.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = visual.supporting,
                        style = MaterialTheme.typography.bodyMedium,
                        color = visual.contentColor.copy(alpha = 0.88f)
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
            containerColor = colorScheme.tertiaryContainer,
            contentColor = colorScheme.onTertiaryContainer,
            iconContainerColor = colorScheme.onTertiaryContainer.copy(alpha = 0.12f),
            iconTint = colorScheme.onTertiaryContainer
        )

        FundingAccountScreenPhase.PENDING -> FundingAccountStateVisual(
            icon = Icons.Filled.Schedule,
            title = stringResource(R.string.funding_account_state_pending_title),
            supporting = stringResource(R.string.funding_account_state_pending_supporting),
            contentDescription = stringResource(R.string.funding_account_status_pending_content_description),
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer,
            iconContainerColor = colorScheme.onSecondaryContainer.copy(alpha = 0.12f),
            iconTint = colorScheme.onSecondaryContainer
        )

        FundingAccountScreenPhase.EMPTY -> FundingAccountStateVisual(
            icon = Icons.Filled.AccountBalance,
            title = stringResource(R.string.funding_account_state_empty_title),
            supporting = stringResource(R.string.funding_account_state_empty_supporting),
            contentDescription = stringResource(R.string.funding_account_status_loading_content_description),
            containerColor = colorScheme.surfaceVariant,
            contentColor = colorScheme.onSurfaceVariant,
            iconContainerColor = colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
            iconTint = colorScheme.onSurfaceVariant
        )

        FundingAccountScreenPhase.KYC_REQUIRED -> FundingAccountStateVisual(
            icon = Icons.Filled.Verified,
            title = stringResource(R.string.funding_account_state_kyc_required_title),
            supporting = stringResource(R.string.funding_account_state_kyc_required_supporting),
            contentDescription = stringResource(R.string.funding_account_status_error_content_description),
            containerColor = colorScheme.surfaceVariant,
            contentColor = colorScheme.onSurfaceVariant,
            iconContainerColor = colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
            iconTint = colorScheme.onSurfaceVariant
        )

        FundingAccountScreenPhase.UNSUPPORTED_MARKET -> FundingAccountStateVisual(
            icon = Icons.Filled.ErrorOutline,
            title = stringResource(R.string.funding_account_state_unsupported_title),
            supporting = stringResource(
                R.string.funding_account_state_unsupported_supporting_format,
                state.countryName
            ),
            contentDescription = stringResource(R.string.funding_account_status_error_content_description),
            containerColor = colorScheme.errorContainer,
            contentColor = colorScheme.onErrorContainer,
            iconContainerColor = colorScheme.onErrorContainer.copy(alpha = 0.12f),
            iconTint = colorScheme.onErrorContainer
        )

        FundingAccountScreenPhase.ERROR -> FundingAccountStateVisual(
            icon = Icons.Filled.ErrorOutline,
            title = stringResource(R.string.funding_account_state_error_title),
            supporting = stringResource(R.string.funding_account_state_error_supporting),
            contentDescription = stringResource(R.string.funding_account_status_error_content_description),
            containerColor = colorScheme.errorContainer,
            contentColor = colorScheme.onErrorContainer,
            iconContainerColor = colorScheme.onErrorContainer.copy(alpha = 0.12f),
            iconTint = colorScheme.onErrorContainer
        )

        FundingAccountScreenPhase.LOADING -> FundingAccountStateVisual(
            icon = Icons.Filled.Schedule,
            title = stringResource(R.string.funding_account_state_loading_title),
            supporting = stringResource(R.string.funding_account_state_loading_supporting),
            contentDescription = stringResource(R.string.funding_account_status_loading_content_description),
            containerColor = colorScheme.surfaceVariant,
            contentColor = colorScheme.onSurfaceVariant,
            iconContainerColor = colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
            iconTint = colorScheme.onSurfaceVariant
        )
    }
}

private data class FundingAccountStateVisual(
    val icon: ImageVector,
    val title: String,
    val supporting: String,
    val contentDescription: String,
    val containerColor: Color,
    val contentColor: Color,
    val iconContainerColor: Color,
    val iconTint: Color
)
