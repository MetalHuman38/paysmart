package net.metalbrain.paysmart.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.EmailVerificationBtn
import net.metalbrain.paysmart.ui.components.FacebookSignInButton
import net.metalbrain.paysmart.ui.components.GoogleSignInBtn
import net.metalbrain.paysmart.ui.viewmodel.LoginViewModel
import net.metalbrain.paysmart.utils.extractSimpleBackendError
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.res.painterResource


@Composable
fun FederatedLinkingScreen(
    navController: NavController,
    onGoogleLinkSuccess: () -> Unit,
    onFacebookLinkSuccess: () -> Unit,
    onSkip: () -> Unit,
    viewModel: LoginViewModel,
) {
    val activity = LocalActivity.current
    val coroutineScope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // No-op
    }
    val backendError = remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    // Background gradient wrapper
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🔐 Main Card with sign-in options
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.thank_you_for_creating_account),
//                        style = MaterialTheme.typography.headlineMedium,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 28.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )

                    Text(
                        text = stringResource(R.string.link_federated_account),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(32.dp))

                    GoogleSignInBtn(
                        launcher = launcher,
                        modifier = Modifier.fillMaxWidth(),
                        onCredentialReceived = {
                            viewModel.linkFederatedCredential(
                                it,
                                onSuccess = onGoogleLinkSuccess,
                                onError = { e ->
                                    backendError.value = extractSimpleBackendError(e)
                                }
                            )
                        },
                        onError = {
                            backendError.value = extractSimpleBackendError(it)
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    FacebookSignInButton(
                        activity = activity ?: return,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.handleFacebookLogin(
                                activity = activity,
                                onSuccess = onFacebookLinkSuccess,
                                onError = { backendError.value = extractSimpleBackendError(it) }
                            )
                        }
                    )


                    Spacer(Modifier.height(16.dp))

                    EmailVerificationBtn(
                        navController = navController,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.skip_federated_linking),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable { onSkip() }
                            .padding(vertical = 8.dp)
                    )
                }

            Spacer(Modifier.height(12.dp))

            // 💡 Explanation Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.Top
            )  {

                Column(
                    modifier = Modifier
                        .weight(2f),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = stringResource(R.string.how_this_works),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Text(
                        text = stringResource(R.string.how_this_works_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }
            Spacer(modifier =  Modifier.height(16.dp))
            // PaySmart and Logo
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
}
