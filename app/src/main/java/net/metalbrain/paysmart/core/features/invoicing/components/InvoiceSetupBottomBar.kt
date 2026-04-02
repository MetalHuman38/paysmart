package net.metalbrain.paysmart.core.features.invoicing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.core.invoice.model.InvoiceFormStep
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens


@Composable
fun InvoiceSetupBottomBar(
    state: InvoiceSetupUiState,
    canContinue: Boolean,
    onContinue: () -> Unit,
    onFinalize: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = Dimens.md, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            Text(
                text = if (state.formStep == InvoiceFormStep.REVIEW) {
                    stringResource(R.string.invoice_setup_footer_review_hint)
                } else {
                    stringResource(R.string.invoice_setup_footer_continue_hint)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PrimaryButton(
                text = if (state.formStep == InvoiceFormStep.REVIEW) {
                    stringResource(R.string.invoice_weekly_finalize_action)
                } else {
                    stringResource(R.string.invoice_setup_continue_action_short)
                },
                onClick = {
                    if (state.formStep == InvoiceFormStep.REVIEW) onFinalize() else onContinue()
                },
                enabled = canContinue && !state.isPersisting && !state.isFinalizing,
                isLoading = state.isPersisting || state.isFinalizing
            )
        }
    }
}
