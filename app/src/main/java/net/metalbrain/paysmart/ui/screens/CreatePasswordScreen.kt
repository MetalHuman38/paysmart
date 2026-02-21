package net.metalbrain.paysmart.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.components.AuthScreenSubtitle
import net.metalbrain.paysmart.ui.components.AuthScreenTitle
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.components.RequirementsList
import net.metalbrain.paysmart.ui.components.StrengthMeter
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.viewmodel.CreatePasswordViewModel
import net.metalbrain.paysmart.utils.evaluatePassword

@Composable
fun CreateLocalPasswordScreen(
    viewModel: CreatePasswordViewModel = hiltViewModel(),
    onDone: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val showContent = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val password = uiState.password
    val confirmPassword = uiState.confirmPassword
    val checks = remember(password) { evaluatePassword(password) }
    val allGood = checks.allPassed && password == confirmPassword

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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
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
                .padding(horizontal = Dimens.screenPadding, vertical = 20.dp)
        ) {
            AnimatedVisibility(
                visible = showContent.value,
                enter = fadeIn(animationSpec = tween(durationMillis = 320)) +
                    slideInVertically(
                        initialOffsetY = { -it / 5 },
                        animationSpec = tween(durationMillis = 320)
                    )
            ) {
                Column {
                    AuthScreenTitle(text = stringResource(R.string.create_secure_password))

                    Spacer(modifier = Modifier.height(8.dp))

                    AuthScreenSubtitle(text = stringResource(R.string.enter_secure_password))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedVisibility(
                visible = showContent.value,
                enter = fadeIn(animationSpec = tween(durationMillis = 420, delayMillis = 80)) +
                    slideInVertically(
                        initialOffsetY = { it / 8 },
                        animationSpec = tween(durationMillis = 420, delayMillis = 80)
                    )
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
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
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = viewModel::onPasswordChanged,
                            label = { Text(stringResource(R.string.password_placeholder)) },
                            singleLine = true,
                            visualTransformation = if (uiState.showPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = viewModel::togglePasswordVisibility) {
                                    Icon(
                                        imageVector = if (uiState.showPassword) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = stringResource(R.string.toggle_password_visibility)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        StrengthMeter(checks)

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = viewModel::onConfirmPasswordChanged,
                            label = { Text(stringResource(R.string.confirm_password_label)) },
                            singleLine = true,
                            isError = confirmPassword.isNotBlank() && password != confirmPassword,
                            visualTransformation = if (uiState.showPassword) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(onClick = viewModel::togglePasswordVisibility) {
                                    Icon(
                                        imageVector = if (uiState.showPassword) {
                                            Icons.Default.Visibility
                                        } else {
                                            Icons.Default.VisibilityOff
                                        },
                                        contentDescription = stringResource(R.string.toggle_password_visibility)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        RequirementsList(checks)

                        if (!uiState.errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            AnimatedVisibility(
                visible = showContent.value,
                enter = fadeIn(animationSpec = tween(durationMillis = 480, delayMillis = 140))
            ) {
                PrimaryButton(
                    text = stringResource(R.string.create_password_cta),
                    onClick = { viewModel.submitPassword(onSuccess = onDone) },
                    enabled = allGood,
                    isLoading = uiState.loading,
                    loadingText = stringResource(R.string.create_password_loading),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
