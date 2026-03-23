package net.metalbrain.paysmart.core.features.fundingaccount.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountScreenPhase
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountUiState
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.LocalAppThemePack

@Composable
fun FundingAccountScreen(
    state: FundingAccountUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onProvision: () -> Unit,
    onCopyAccountNumber: () -> Unit,
    onShareDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themePack = LocalAppThemePack.current
    val palette = if (MaterialTheme.colorScheme.background.red < 0.2f) {
        themePack.darkBackground
    } else {
        themePack.lightBackground
    }

    LazyColumn(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        palette.start,
                        palette.accentOne,
                        palette.accentTwo,
                        palette.end
                    )
                )
            ),
        contentPadding = PaddingValues(
            start = Dimens.screenPadding,
            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + Dimens.md,
            end = Dimens.screenPadding,
            bottom = Dimens.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.lg)
    ) {
        item {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.94f),
                tonalElevation = 4.dp
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(Dimens.xs)
            ) {
                Text(
                    text = stringResource(R.string.funding_account_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (state.phase == FundingAccountScreenPhase.UNSUPPORTED_MARKET) {
                        stringResource(
                            R.string.funding_account_state_unsupported_supporting_format,
                            state.countryName
                        )
                    } else {
                        stringResource(R.string.feature_gate_receive_money_description)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            FundingAccountContent(
                state = state,
                onRefresh = onRefresh,
                onProvision = onProvision,
                onCopyAccountNumber = onCopyAccountNumber,
                onShareDetails = onShareDetails
            )
        }
    }
}
