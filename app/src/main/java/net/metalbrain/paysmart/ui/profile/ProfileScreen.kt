package net.metalbrain.paysmart.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileScreen(
    user: AuthUserModel,
    viewModel: UserViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 🧑 Profile Avatar, Name, Email
            ProfileHeader(
                displayName = user.displayName,
                email = user.email,
                isVerified = true, // 🔐 optional check on your side
            )

            // 📋 Menu List
            ProfileMenuItem("Account information", "Information about your account") { }
            ProfileMenuItem("Help and support", "Need help? We’ve got you.") { }
            ProfileMenuItem("Security and privacy", "Keep your account safe") { }
            ProfileMenuItem("Notification preferences", "Manage your notifications") { }
            ProfileMenuItem("Manage connected accounts", "External accounts connected") { }
            ProfileMenuItem("About", "Information about this app") { }

            Spacer(modifier = Modifier.weight(1f))

            // 🚪 Logout
            TextButton(
                onClick = {
                    viewModel.signOut()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Log out", color = MaterialTheme.colorScheme.error)
            }

            Text(
                text = "Version: 1.0.0 (100)",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
