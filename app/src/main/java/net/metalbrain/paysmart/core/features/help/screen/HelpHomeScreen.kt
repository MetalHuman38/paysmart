package net.metalbrain.paysmart.core.features.help.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileMenuItem
import net.metalbrain.paysmart.core.features.help.viewmodel.HelpUiState
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun HelpHomeScreen(
    uiState: HelpUiState,
    onBack: () -> Unit,
    onHelpCenterClick: () -> Unit,
    onContactSupportClick: () -> Unit,
    onCallCenterClick: () -> Unit,
    onSocialMediaClick: () -> Unit
) {
    val greeting = if (uiState.firstName.isNotBlank()) {
        stringResource(R.string.help_home_greeting_format, uiState.firstName)
    } else {
        stringResource(R.string.help_home_greeting_fallback)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = Dimens.screenPadding, vertical = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back)
                )
            }

            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ProfileMenuItem(
                        title = stringResource(R.string.help_home_help_center_title),
                        subtitle = stringResource(R.string.help_home_help_center_subtitle),
                        leadingIcon = Icons.Default.Description,
                        leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onHelpCenterClick
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        title = stringResource(R.string.help_home_contact_support_title),
                        subtitle = stringResource(R.string.help_home_contact_support_subtitle),
                        leadingIcon = Icons.Default.AlternateEmail,
                        leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onContactSupportClick
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        title = stringResource(R.string.help_home_call_center_title),
                        subtitle = stringResource(R.string.help_home_call_center_subtitle),
                        leadingIcon = Icons.Default.Call,
                        leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onCallCenterClick
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        title = stringResource(R.string.help_home_socials_title),
                        subtitle = stringResource(R.string.help_home_socials_subtitle),
                        leadingIcon = Icons.Default.Public,
                        leadingIconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onSocialMediaClick
                    )
                }
            }
        }
    }
}
