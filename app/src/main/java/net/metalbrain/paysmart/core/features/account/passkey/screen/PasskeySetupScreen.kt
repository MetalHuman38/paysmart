package net.metalbrain.paysmart.core.features.account.passkey.screen

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.passkey.viewmodel.PasskeySetupViewModel
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun PasskeySetupScreen(
    activity: Activity,
    viewModel: PasskeySetupViewModel,
    onRegistered: () -> Unit = {},
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) {
            onRegistered()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.passkey_setup_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.passkey_setup_description),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = if (state.isRegistered) {
                stringResource(R.string.passkey_registered)
            } else {
                stringResource(R.string.passkey_create_action)
            },
            onClick = { viewModel.registerPasskey(activity) },
            enabled = !state.isLoading && !state.isRegistered,
            isLoading = state.isLoading,
            loadingText = stringResource(R.string.common_processing)
        )

        Spacer(modifier = Modifier.height(12.dp))

        PrimaryButton(
            text = stringResource(R.string.passkey_verify_action),
            onClick = { viewModel.verifyPasskey(activity) },
            enabled = !state.isLoading
        )

        state.statusMessage?.let { status ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = status, style = MaterialTheme.typography.bodySmall)
        }

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.common_back))
        }
    }
}
