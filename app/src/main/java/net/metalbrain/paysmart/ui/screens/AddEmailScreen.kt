package net.metalbrain.paysmart.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.components.EmailInputField
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.AddEmailViewModel


@Composable
fun AddEmailScreen(
    navController: NavController,
    viewModel: AddEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // 🌀 Global loading screen
    if (uiState.loading) {
        AppLoadingScreen(message = "Sending verification email…")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = Dimens.largeScreenPadding)
            .padding(horizontal = Dimens.screenPadding)
    ) {

        Text(
            text = stringResource(R.string.verify_your_email),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.why_verifying_your_email),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        EmailInputField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            isError = uiState.email.isNotBlank() && !uiState.emailValid,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.error != null) {
            Spacer(Modifier.height(8.dp))
            Text(uiState.error!!, color = Color.Red)
        }

        Spacer(Modifier.height(16.dp))

        PrimaryButton(
            text = stringResource(R.string.send_verification),
            onClick = {
                viewModel.sendVerificationEmail {
                    navController.navigate(
                        Screen.EmailSent.routeWithEmail(uiState.email)
                    )
                }
            },
            enabled = uiState.emailValid,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
