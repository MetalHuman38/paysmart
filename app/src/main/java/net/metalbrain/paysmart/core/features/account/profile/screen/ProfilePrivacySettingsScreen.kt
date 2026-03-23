package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileSecurityActionRow
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileSecurityToggleRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePrivacySettingsScreen(
    privacyCreditEnabled: Boolean,
    privacySocialMediaEnabled: Boolean,
    onBack: () -> Unit,
    onPrivacyCreditToggle: (Boolean) -> Unit,
    onPrivacySocialMediaToggle: (Boolean) -> Unit,
    onOpenTermsOfUse: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_security_view_privacy_settings)) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        ProfileSecurityToggleRow(
                            title = stringResource(R.string.credit),
                            icon = Icons.Default.CreditCard,
                            subtitle = stringResource(R.string.i_am_happy_paysmart_share_my_data),
                            checked = privacyCreditEnabled,
                            onCheckedChange = onPrivacyCreditToggle
                        )
                        HorizontalDivider()
                        ProfileSecurityToggleRow(
                            title = stringResource(R.string.profile_about_social_media_title),
                            icon = Icons.Outlined.Public,
                            subtitle = stringResource(R.string.social_media_advertisement_platform),
                            checked = privacySocialMediaEnabled,
                            onCheckedChange = onPrivacySocialMediaToggle
                        )
                        HorizontalDivider()
                        ProfileSecurityActionRow(
                            title = stringResource(R.string.profile_about_legal_title),
                            icon = Icons.Default.Description,
                            subtitle = stringResource(R.string.terms_of_use),
                            onClick = onOpenTermsOfUse
                        )
                    }
                }
            }
        }
    }
}
