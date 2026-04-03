package net.metalbrain.paysmart.core.features.account.authorization.passcode.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.feature.account.R
import net.metalbrain.paysmart.core.features.account.authorization.passcode.card.BrandFooter
import net.metalbrain.paysmart.core.features.account.authorization.passcode.card.PasscodeSetupCard
import net.metalbrain.paysmart.core.features.account.authorization.passcode.viewmodel.PasscodeViewModel
import net.metalbrain.paysmart.ui.components.AuthScreenSubtitle
import net.metalbrain.paysmart.ui.components.AuthScreenTitle
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun SetPasscodeScreen(
    viewModel: PasscodeViewModel = hiltViewModel(),
    onPasscodeSet: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val passcode = uiState.passcode
    val confirm = uiState.confirmPasscode
    val passcodesMismatch = confirm.isNotBlank() && passcode != confirm
    val canSubmit = passcode.length >= 4 && passcode == confirm
    var showPasscode by remember { mutableStateOf(false) }
    var showConfirmPasscode by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(Dimens.lg)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                ) {
                    AuthScreenTitle(text = stringResource(R.string.set_up_your_pass_code))
                    AuthScreenSubtitle(text = stringResource(R.string.passcode_use_to_unlock))
                }

                PasscodeSetupCard(
                    passcode = passcode,
                    confirm = confirm,
                    showPasscode = showPasscode,
                    showConfirmPasscode = showConfirmPasscode,
                    isMismatch = passcodesMismatch,
                    error = uiState.error,
                    isLoading = uiState.loading,
                    onPasscodeChange = viewModel::onPasscodeChanged,
                    onConfirmChange = viewModel::onConfirmPasscodeChanged,
                    onTogglePasscode = { showPasscode = !showPasscode },
                    onToggleConfirmPasscode = { showConfirmPasscode = !showConfirmPasscode },
                    onSubmit = { viewModel.submitPasscode(onSuccess = onPasscodeSet) },
                    canSubmit = canSubmit
                )
            }

            BrandFooter()
        }
    }
}
