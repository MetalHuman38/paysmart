package net.metalbrain.paysmart.core.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.metalbrain.paysmart.ui.viewmodel.SecurityViewModel
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.ui.components.PrimaryButton
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
fun PasscodePrompt(
    user: AuthUserModel,
    onVerified: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val viewModel: SecurityViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // User Info
        Text(
            text = "Hi ${user.displayName ?: "there"}",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 🔐 Subtitle
        Text(
            text = "Enter your 4-digit passcode to log in",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(text = stringResource(R.string.enter_your_passcode))

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = code,
            onValueChange = {
                if (it.length <= 6) code = it.filter { char -> char.isDigit() }
            },
            label = { Text("Passcode") },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(imageVector = image, contentDescription = if (isPasswordVisible) "Hide passcode" else "Show passcode")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = stringResource(R.string.passcode_use_to_unlock))

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryButton(
            text = stringResource(R.string.continue_text),
            onClick = {
            coroutineScope.launch {
                val success = viewModel.verify(code)
                if (success) {
                    onVerified()
                }
            }
        }
        )
    }
}
