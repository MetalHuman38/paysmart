package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenPadding, vertical = Dimens.md)
            ) {
                PrimaryButton(
                    text = stringResource(R.string.identity_resolver_done_action),
                    onClick = onDone
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            IdentityFlowHeader(
                title = stringResource(R.string.identity_pending_title),
                subtitle = stringResource(R.string.identity_pending_subtitle),
                onBack = onDone,
                onHelp = onHelp
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    Text(
                        text = stringResource(R.string.identity_resolver_review_time_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(
                            R.string.identity_pending_waiting_body,
                            selectedCountry.name,
                            selectedCountry.reviewWindowLabel
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    Text(
                        text = stringResource(R.string.identity_resolver_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(R.string.identity_pending_status_format, formattedStatus),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    HorizontalDivider()
                    receipt?.verificationId?.takeIf { it.isNotBlank() }?.let { verificationId ->
                        Text(
                            text = stringResource(
                                R.string.identity_pending_reference_format,
                                verificationId
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${selectedCountry.flag} ${selectedCountry.iso2}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimens.lg),
                    verticalArrangement = Arrangement.spacedBy(Dimens.md)
                ) {
                    Text(
                        text = stringResource(R.string.identity_pending_next_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    PendingStep(text = stringResource(R.string.identity_pending_next_step_1))
                    PendingStep(text = stringResource(R.string.identity_pending_next_step_2))
                    PendingStep(text = stringResource(R.string.identity_pending_next_step_3))
                }
            }
        }
    }
}

@Composable
private fun PendingStep(text: String) {
    Text(
        text = "- $text",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
