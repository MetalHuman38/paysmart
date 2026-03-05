package net.metalbrain.paysmart.core.features.identity.screen

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityProviderHandoffViewModel
import net.metalbrain.paysmart.ui.components.OutlinedButton
import net.metalbrain.paysmart.ui.components.PrimaryButton

@Composable
fun IdentityThirdPartyProviderScreen(
    viewModel: IdentityProviderHandoffViewModel,
    callbackEvent: String,
    callbackSessionId: String?,
    callbackProviderRef: String?,
    callbackDeepLink: String?,
    onBack: () -> Unit,
    onFallbackToLocal: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(callbackEvent, callbackSessionId, callbackProviderRef, callbackDeepLink) {
        viewModel.consumeCallbackArgs(
            event = callbackEvent,
            sessionId = callbackSessionId,
            providerRef = callbackProviderRef,
            deepLink = callbackDeepLink
        )
    }

    Scaffold(topBar = { IdentityResolverTopBar(onBack = onBack) }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.identity_provider_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = stringResource(R.string.identity_provider_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                stringResource(R.string.identity_provider_value, state.provider)
            )
            Text(
                stringResource(
                    R.string.identity_provider_session_value,
                    state.sessionId ?: stringResource(R.string.identity_provider_not_started)
                )
            )
            Text(
                stringResource(R.string.identity_provider_status_value, state.status)
            )
            state.lastEvent?.let {
                Text(stringResource(R.string.identity_provider_callback_event_value, it))
            }
            state.reason?.let {
                Text(stringResource(R.string.identity_provider_reason_value, it))
            }

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }
            state.info?.let {
                Text(text = it, color = MaterialTheme.colorScheme.primary)
            }

            PrimaryButton(
                text = stringResource(R.string.identity_provider_start_action),
                onClick = { viewModel.startSession(countryIso2 = null, documentType = null) },
                enabled = !state.isBusy,
                isLoading = state.isStartingSession
            )

            PrimaryButton(
                text = stringResource(R.string.identity_provider_resume_action),
                onClick = viewModel::resumeSession,
                enabled = state.hasSession && !state.isBusy,
                isLoading = state.isResuming
            )

            PrimaryButton(
                text = stringResource(R.string.identity_provider_open_action),
                onClick = {
                    state.launchUrl?.let { url ->
                        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    }
                },
                enabled = !state.launchUrl.isNullOrBlank() && !state.isBusy
            )

            OutlinedButton(
                text = stringResource(R.string.identity_resolver_capture_step),
                onClick = onFallbackToLocal,
                enabled = !state.isBusy
            )

            OutlinedButton(
                text = stringResource(R.string.dismiss),
                onClick = {
                    viewModel.clearMessages()
                    onBack()
                },
                enabled = !state.isBusy
            )
        }
    }
}
