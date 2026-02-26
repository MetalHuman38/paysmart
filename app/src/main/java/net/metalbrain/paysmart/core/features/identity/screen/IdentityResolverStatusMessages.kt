package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.state.IdentitySetupResolverUiState
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityResolverStep

@Composable
fun IdentityResolverStatusMessages(state: IdentitySetupResolverUiState) {
    state.error?.takeIf { it.isNotBlank() }?.let { message ->
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    state.nameMatchWarning?.takeIf { it.isNotBlank() }?.let { warning ->
        Text(
            text = warning,
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    if (state.isValidatingCapture) {
        Text(
            text = stringResource(R.string.identity_resolver_capture_validating),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (state.currentStep == IdentityResolverStep.COMPLETE) {
        Text(
            text = stringResource(R.string.identity_resolver_completed_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
