package net.metalbrain.paysmart.ui.account.recovery.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.ui.account.recovery.components.RecoveryOptionItem

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RecoverAccountScreen(
    onBackClick: () -> Unit,
    onHelpClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onChangePhoneClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.recover_your_account),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onHelpClick) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Forgot Password Option
                RecoveryOptionItem(
                    icon = painterResource(id = R.drawable.ic_password), // Replace with actual icon
                    title = stringResource(R.string.forgot_password_title),
                    description = stringResource(R.string.forgot_password_description),
                    buttonText = stringResource(R.string.change_password),
                    onClick = onChangePasswordClick
                )

                // Change Phone Number Option
                RecoveryOptionItem(
                    icon = painterResource(id = R.drawable.ic_phone), // Replace with actual icon
                    title = stringResource(R.string.change_phone_title),
                    description = stringResource(R.string.change_phone_description),
                    buttonText = stringResource(R.string.change_phone_number),
                    onClick = onChangePhoneClick
                )
            }
        }
    )
}
