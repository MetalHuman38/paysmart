package net.metalbrain.paysmart.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.BackendErrorModal
import net.metalbrain.paysmart.ui.components.EmailVerificationBtn
import net.metalbrain.paysmart.ui.components.FacebookSignInButton
import net.metalbrain.paysmart.ui.components.GoogleSignInBtn
import net.metalbrain.paysmart.ui.viewmodel.LoginViewModel
import net.metalbrain.paysmart.utils.extractSimpleBackendError


@Composable
fun FederatedLinkingScreen(
    navController: NavController,
    onGoogleLinkSuccess: () -> Unit,
    onFacebookLinkSuccess: () -> Unit,
    onSkip: () -> Unit,
    viewModel: LoginViewModel,
) {
    val activity = LocalActivity.current
    val isAuthLoading = viewModel.loading
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        // No-op
    }
    val backendError = remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val showContent = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showContent.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showContent.value,
                enter = fadeIn(animationSpec = tween(durationMillis = 350)) +
                    slideInVertically(
                        initialOffsetY = { -it / 4 },
                        animationSpec = tween(durationMillis = 350)
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(68.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_paysmart_logo),
                                contentDescription = "PaySmart logo",
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.thank_you_for_creating_account),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.link_federated_account),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(
                visible = showContent.value,
                enter = fadeIn(animationSpec = tween(durationMillis = 420, delayMillis = 80)) +
                    slideInVertically(
                        initialOffsetY = { it / 6 },
                        animationSpec = tween(durationMillis = 420, delayMillis = 80)
                    )
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        GoogleSignInBtn(
                            launcher = launcher,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isAuthLoading,
                            isLoading = isAuthLoading,
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

                        Spacer(Modifier.height(12.dp))

                        if (activity != null) {
                            FacebookSignInButton(
                                activity = activity,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isAuthLoading,
                                isLoading = isAuthLoading,
                                onClick = {
                                    viewModel.handleFacebookLogin(
                                        activity = activity,
                                        onSuccess = onFacebookLinkSuccess,
                                        onError = { backendError.value = extractSimpleBackendError(it) }
                                    )
                                }
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        EmailVerificationBtn(
                            navController = navController,
                            modifier = Modifier.fillMaxWidth()
                        )

                        backendError.value?.let { error ->
                            Spacer(Modifier.height(12.dp))
                            BackendErrorModal(
                                message = error,
                                onDismiss = { backendError.value = null }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.skip_federated_linking),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier
                                .clickable(enabled = !isAuthLoading) { onSkip() }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(
                visible = showContent.value,
                enter = fadeIn(animationSpec = tween(durationMillis = 460, delayMillis = 160)) +
                    slideInVertically(
                        initialOffsetY = { it / 8 },
                        animationSpec = tween(durationMillis = 460, delayMillis = 160)
                    )
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                        Text(
                            text = stringResource(R.string.how_this_works),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.how_this_works_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = showContent.value,
                enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 240))
            ) {
                Text(
                    text = "PaySmart",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}
