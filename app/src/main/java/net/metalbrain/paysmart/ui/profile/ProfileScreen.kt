package net.metalbrain.paysmart.ui.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.security.RoomKeyManager
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileScreen(
    user: AuthUserModel,
    isVerified: Boolean,
    viewModel: UserViewModel,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    val menuEntries = remember {
        listOf(
            "Account information" to "Information about your account",
            "Security and privacy" to "Keep your account safe",
            "Manage connected accounts" to "External accounts connected",
            "Help and support" to "Need help? We have got you",
            "About" to "Information about this app"
        )
    }
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn() + slideInVertically { it / 12 },
            exit = fadeOut() + slideOutVertically { it / 12 }
        ) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileHeader(
                    displayName = user.displayName.orEmpty(),
                    photoURL = user.photoURL,
                    isVerified = isVerified
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        menuEntries.forEachIndexed { index, (title, subtitle) ->
                            ProfileMenuItem(
                                title = title,
                                subtitle = subtitle,
                                onClick = {}
                            )
                            if (index < menuEntries.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        viewModel.signOut()
                        onLogout()
                        RoomKeyManager.deleteKey()
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
}
