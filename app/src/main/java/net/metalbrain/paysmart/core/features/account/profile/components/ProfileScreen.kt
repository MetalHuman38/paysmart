package net.metalbrain.paysmart.core.features.account.profile.components

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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.BuildConfig
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.room.manager.RoomKeyManager
import net.metalbrain.paysmart.domain.model.AuthUserModel
import net.metalbrain.paysmart.ui.viewmodel.UserViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileScreen(
    user: AuthUserModel,
    isVerified: Boolean,
    viewModel: UserViewModel,
    onAccountInformationClick: () -> Unit,
    onSecurityPrivacyClick: () -> Unit,
    onConnectedAccountsClick: () -> Unit,
    onHelpAndSupportClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {
    val menuEntries = listOf(
        ProfileMenuEntry(
            title = stringResource(R.string.profile_menu_account_information_title),
            subtitle = stringResource(R.string.profile_menu_account_information_subtitle),
            icon = Icons.Default.Person,
            onClick = onAccountInformationClick
        ),
        ProfileMenuEntry(
            title = stringResource(R.string.profile_menu_security_privacy_title),
            subtitle = stringResource(R.string.profile_menu_security_privacy_subtitle),
            icon = Icons.Default.Security,
            onClick = onSecurityPrivacyClick
        ),
        ProfileMenuEntry(
            title = stringResource(R.string.profile_menu_connected_accounts_title),
            subtitle = stringResource(R.string.profile_menu_connected_accounts_subtitle),
            icon = Icons.Default.Link,
            onClick = onConnectedAccountsClick
        ),
        ProfileMenuEntry(
            title = stringResource(R.string.profile_menu_help_support_title),
            subtitle = stringResource(R.string.profile_menu_help_support_subtitle),
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            onClick = onHelpAndSupportClick
        ),
        ProfileMenuEntry(
            title = stringResource(R.string.profile_menu_about_title),
            subtitle = stringResource(R.string.profile_menu_about_subtitle),
            icon = Icons.Default.Info,
            onClick = onAboutClick
        )
    )
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
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
                        menuEntries.forEachIndexed { index, entry ->
                            ProfileMenuItem(
                                title = entry.title,
                                subtitle = entry.subtitle,
                                leadingIcon = entry.icon,
                                onClick = entry.onClick
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
                    Text(
                        text = stringResource(R.string.profile_logout),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Text(
                    text = stringResource(
                        R.string.profile_version_format,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
