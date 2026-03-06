package net.metalbrain.paysmart.core.features.referral.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun ReferralActionButtons(
    canSubmit: Boolean,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    onNoCode: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PrimaryButton(
            text = stringResource(R.string.referral_submit_action),
            onClick = onSubmit,
            enabled = canSubmit,
            isLoading = isSubmitting,
            loadingText = stringResource(R.string.common_processing),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedButton(
            text = stringResource(R.string.referral_no_code_action),
            onClick = onNoCode,
            enabled = !isSubmitting,
            contentColor = MaterialTheme.colorScheme.onSurface,
            borderColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

