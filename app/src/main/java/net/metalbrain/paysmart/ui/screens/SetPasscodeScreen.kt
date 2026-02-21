package net.metalbrain.paysmart.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.PasscodeField
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.PasscodeViewModel

@Composable
fun SetPasscodeScreen(
    viewModel: PasscodeViewModel = hiltViewModel(),
    onPasscodeSet: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    val passcode = uiState.passcode
    val confirm = uiState.confirmPasscode
    var showPasscode by remember { mutableStateOf(false) }
    var showConfirmPasscode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = Dimens.largeScreenPadding)
            .padding(horizontal = Dimens.screenPadding)

    ) {
        Text(stringResource(
            id = R.string.set_up_your_pass_code),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant

        )

        Spacer(Modifier.height(8.dp))

        Text(stringResource(
            id = R.string.passcode_use_to_unlock),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary

        )

        Spacer(Modifier.height(24.dp))

        PasscodeField(
            value = passcode,
            onValueChange = viewModel::onPasscodeChanged,
            label = "Enter passcode",
            showText = showPasscode,
            onToggleVisibility = { showPasscode = !showPasscode }
        )

        Spacer(Modifier.height(16.dp))

        PasscodeField(
            value = confirm,
            onValueChange = viewModel::onConfirmPasscodeChanged,
            label = "Confirm passcode",
            showText = showConfirmPasscode,
            onToggleVisibility = { showConfirmPasscode = !showConfirmPasscode },
            isError = confirm.isNotBlank() && passcode != confirm
        )

        if (uiState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = "Save passcode",
            onClick = {
                Log.d("SetPasscodeScreen", "Save button clicked")
                viewModel.submitPasscode(onSuccess = onPasscodeSet)
                      },
            enabled = passcode.length >= 4 && passcode == confirm,
            isLoading = uiState.loading,
            loadingText = "Saving...",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_paysmart_logo),
                contentDescription = "PaySmart Logo",
                modifier = Modifier.height(34.dp)
            )

            Spacer(modifier = Modifier.width(2.dp))

            Text(
                text = "PaySmart",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
