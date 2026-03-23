package net.metalbrain.paysmart.core.features.account.authentication.email.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.authentication.email.viewmodel.AddEmailViewModel
import net.metalbrain.paysmart.navigator.Screen
import net.metalbrain.paysmart.ui.components.AuthScreenSubtitle
import net.metalbrain.paysmart.ui.components.AuthScreenTitle
import net.metalbrain.paysmart.ui.components.EmailInputField
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun AddEmailScreen(
    navController: NavController,
    returnRoute: String = Screen.Home.route,
    viewModel: AddEmailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(top = Dimens.largeScreenPadding)
            .padding(horizontal = Dimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        AuthScreenTitle(
            text = stringResource(R.string.verify_your_email),
        )

        AuthScreenSubtitle(
            text = stringResource(R.string.why_verifying_your_email),
            modifier = Modifier.padding(bottom = Dimens.sm)
        )

        EmailInputField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChanged,
            isError = uiState.email.isNotBlank() && !uiState.emailValid,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        PrimaryButton(
            text = stringResource(R.string.send_verification),
            onClick = {
                viewModel.sendVerificationEmail(returnRoute = returnRoute) {
                    val targetRoute =
                        Screen.EmailSent.routeWithArgs(
                            email = uiState.email,
                            returnRoute = returnRoute
                        )
                    navController.navigate(
                        targetRoute
                    )
                    viewModel.onEmailChanged("")
                }
            },
            enabled = uiState.emailValid,
            isLoading = uiState.loading,
            loadingText = stringResource(R.string.common_processing),
            modifier = Modifier.fillMaxWidth()
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimens.lg),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_paysmart_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.height(34.dp)
            )

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(Dimens.xs))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
