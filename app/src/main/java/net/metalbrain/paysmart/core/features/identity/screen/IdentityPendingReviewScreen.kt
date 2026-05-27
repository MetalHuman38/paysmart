package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentitySetupResolverViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun IdentityPendingReviewScreen(
    viewModel: IdentitySetupResolverViewModel,
    onDone: () -> Unit,
    onHelp: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val selectedCountry = remember(state.selectedCountryIso2, state.selectedCountryReviewWindow) {
        resolveIdentityCountryPresentation(context, state.selectedCountryIso2)
    }
    val receipt = state.receipt
    val formattedStatus = receipt?.status
        ?.replace('_', ' ')
        ?.lowercase()
        ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        ?: stringResource(R.string.identity_resolver_status_pending)
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

    Scaffold(
        containerColor = colors.backgroundPrimary
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            IdentityFlowHeader(
                title = stringResource(R.string.identity_pending_title),
                subtitle = stringResource(R.string.identity_pending_subtitle),
                onBack = onDone,
                onHelp = onHelp
            )

            IdentityReviewCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    Text(
                        text = stringResource(R.string.identity_resolver_review_time_title),
                        style = typography.heading4,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(
                            R.string.identity_pending_waiting_body,
                            selectedCountry.name,
                            selectedCountry.reviewWindowLabel
                        ),
                        style = typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }

            IdentityReviewCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    Text(
                        text = stringResource(R.string.identity_resolver_title),
                        style = typography.heading4,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.identity_pending_status_format, formattedStatus),
                        style = typography.bodyMedium,
                        color = colors.textPrimary
                    )
                    HorizontalDivider()
                    receipt?.verificationId?.takeIf { it.isNotBlank() }?.let { verificationId ->
                        Text(
                            text = stringResource(
                                R.string.identity_pending_reference_format,
                                verificationId
                            ),
                            style = typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                    Text(
                        text = "${selectedCountry.flag} ${selectedCountry.iso2}",
                        style = typography.bodyMedium,
                        color = colors.textSecondary
                    )
                }
            }

            IdentityReviewCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    Text(
                        text = stringResource(R.string.identity_pending_next_title),
                        style = typography.heading4,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    PendingStep(text = stringResource(R.string.identity_pending_next_step_1))
                    PendingStep(text = stringResource(R.string.identity_pending_next_step_2))
                    PendingStep(text = stringResource(R.string.identity_pending_next_step_3))
                }
            }

            PrimaryButton(
                text = stringResource(R.string.identity_resolver_done_action),
                onClick = onDone
            )

            Spacer(modifier = Modifier.height(Dimens.xl))
        }
    }
}

@Composable
private fun IdentityReviewCard(content: @Composable () -> Unit) {
    val colors = PaysmartTheme.colorTokens
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PaysmartTheme.radius.medium,
        color = colors.surfaceElevated,
        contentColor = colors.textPrimary,
        tonalElevation = PaysmartTheme.elevation.subtle,
        border = BorderStroke(PaysmartTheme.border.thin, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            content = { content() }
        )
    }
}

@Composable
private fun PendingStep(text: String) {
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens
    Text(
        text = "- $text",
        style = typography.bodyMedium,
        color = colors.textSecondary
    )
}
