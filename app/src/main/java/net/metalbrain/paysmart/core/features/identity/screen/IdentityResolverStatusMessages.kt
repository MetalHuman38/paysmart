package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.state.IdentitySetupResolverUiState
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityResolverStep
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme

@Composable
fun IdentityResolverStatusMessages(state: IdentitySetupResolverUiState) {
    val colors = PaysmartTheme.colorTokens
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
        state.error?.takeIf { it.isNotBlank() }?.let { message ->
            IdentityStatusCard(
                message = message,
                containerColor = colors.error.copy(alpha = 0.16f),
                contentColor = colors.error
            )
        }

        state.nameMatchWarning?.takeIf { it.isNotBlank() }?.let { warning ->
            IdentityStatusCard(
                message = warning,
                containerColor = colors.warning.copy(alpha = 0.16f),
                contentColor = colors.warning
            )
        }

        if (state.isValidatingCapture) {
            IdentityStatusCard(
                message = stringResource(R.string.identity_resolver_capture_validating),
                containerColor = colors.info.copy(alpha = 0.16f),
                contentColor = colors.info
            )
        }

        if (state.currentStep == IdentityResolverStep.COMPLETE) {
            IdentityStatusCard(
                message = stringResource(R.string.identity_resolver_completed_message),
                containerColor = colors.success.copy(alpha = 0.16f),
                contentColor = colors.success
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PaysmartTheme.radius.medium,
        color = containerColor,
        contentColor = contentColor,
        border = BorderStroke(PaysmartTheme.border.thin, contentColor.copy(alpha = 0.18f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(Dimens.md),
            color = contentColor,
            style = PaysmartTheme.typographyTokens.bodyMedium
        )
    }
}
