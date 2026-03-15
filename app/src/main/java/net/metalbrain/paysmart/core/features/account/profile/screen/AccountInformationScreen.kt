package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.components.ProfileMenuItem
import net.metalbrain.paysmart.core.features.account.profile.util.accountInformationLanguageLabel
import net.metalbrain.paysmart.core.features.account.profile.util.accountInformationThemeModeRes
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountrySelectionCatalog
import net.metalbrain.paysmart.core.features.theme.data.AppThemeMode


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountInformationScreen(
    currentLanguage: String,
    currentThemeMode: AppThemeMode,
    profileStatusLabel: String,
    onBack: () -> Unit,
    onProfileClick: () -> Unit,
    onAccountLimitsClick: () -> Unit,
    onAccountStatementClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onThemeModeClick: () -> Unit
) {
    val context = LocalContext.current
    val languageLabel = accountInformationLanguageLabel(currentLanguage)
    val languageFlag = CountrySelectionCatalog.flagForCountry(
        context = context,
        rawIso2 = languageLabel.countryIso2
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_account_information_title)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ProfileMenuItem(
                        title = stringResource(R.string.profile_details_title),
                        subtitle = stringResource(R.string.profile_account_info_profile_subtitle),
                        leadingIcon = Icons.Default.Person,
                        trailingText = profileStatusLabel,
                        onClick = onProfileClick
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        title = stringResource(R.string.account_limits_title),
                        subtitle = stringResource(R.string.profile_account_info_limits_subtitle),
                        leadingIcon = Icons.Default.AccountBalanceWallet,
                        onClick = onAccountLimitsClick
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        title = stringResource(R.string.account_statement_title),
                        subtitle = stringResource(R.string.profile_account_info_statement_subtitle),
                        leadingIcon = Icons.Default.Description,
                        onClick = onAccountStatementClick
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        title = stringResource(R.string.profile_language_title),
                        subtitle = stringResource(R.string.profile_account_info_language_subtitle),
                        leadingIcon = Icons.Default.Language,
                        trailingText = "$languageFlag ${stringResource(languageLabel.nameRes)}",
                        onClick = onLanguageClick
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        title = stringResource(R.string.profile_theme_title),
                        subtitle = stringResource(R.string.profile_account_info_theme_subtitle),
                        leadingIcon = Icons.Default.DarkMode,
                        trailingText = stringResource(accountInformationThemeModeRes(currentThemeMode)),
                        onClick = onThemeModeClick
                    )
                }
            }
        }
    }
}
