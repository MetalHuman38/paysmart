package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.state.IdentitySetupResolverUiState
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityResolverStep
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun IdentityResolverStatusMessages(state: IdentitySetupResolverUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
        state.error?.takeIf { it.isNotBlank() }?.let { message ->
            IdentityStatusCard(
                message = message,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        state.nameMatchWarning?.takeIf { it.isNotBlank() }?.let { warning ->
            IdentityStatusCard(
                message = warning,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        if (state.isValidatingCapture) {
            IdentityStatusCard(
                message = stringResource(R.string.identity_resolver_capture_validating),
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        if (state.currentStep == IdentityResolverStep.COMPLETE) {
            IdentityStatusCard(
                message = stringResource(R.string.identity_resolver_completed_message),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun IdentityStatusCard(
    message: String,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(Dimens.md),
            color = contentColor,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
