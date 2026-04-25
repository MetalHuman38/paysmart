package net.metalbrain.paysmart.core.features.account.authentication.email.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.ui.R as CoreUiR
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.ui.components.AuthScreenSubtitle
import net.metalbrain.paysmart.ui.components.AuthScreenTitle
import net.metalbrain.paysmart.ui.components.EmailInputField
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun AddEmailScreen(
    email: String,
    emailValid: Boolean,
    error: String?,
    isLoading: Boolean,
    onEmailChanged: (String) -> Unit,
    onSendVerification: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = Dimens.largeScreenPadding)
            .padding(horizontal = Dimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        AuthScreenTitle(text = stringResource(R.string.verify_your_email))

        AuthScreenSubtitle(
            text = stringResource(R.string.why_verifying_your_email),
            modifier = Modifier.padding(bottom = Dimens.sm)
        )

        EmailInputField(
            value = email,
            onValueChange = onEmailChanged,
            isError = email.isNotBlank() && !emailValid,
            modifier = Modifier.fillMaxWidth()
        )

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        PrimaryButton(
            text = stringResource(R.string.send_verification),
            onClick = onSendVerification,
            enabled = emailValid,
            isLoading = isLoading,
            loadingText = stringResource(R.string.common_processing),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.lg),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = CoreUiR.drawable.ic_paysmart_logo),
                contentDescription = null,
                modifier = Modifier.height(34.dp)
            )
            Spacer(modifier = Modifier.width(Dimens.xs))
            Text(
                text = "PaySmart",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
