package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.card.AccountLimitsSelectorCard
import net.metalbrain.paysmart.core.features.account.profile.card.AccountLimitsSyncCard
import net.metalbrain.paysmart.core.features.capabilities.state.AccountLimitsListUiState
import net.metalbrain.paysmart.domain.model.LocalSecuritySettingsModel
import net.metalbrain.paysmart.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountLimitsScreen(
    state: AccountLimitsListUiState,
    settings: LocalSecuritySettingsModel?,
    onBack: () -> Unit,
    onAccountClick: (String) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.account_limits_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(
                    horizontal = Dimens.screenPadding,
                    vertical = Dimens.lg
                )
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {

            Text(
                text = stringResource(R.string.select_account_limits_to_see_or_receive),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            AccountLimitsSelectorCard(
                modifier = Modifier.padding(top = Dimens.lg),
                accounts = state.accounts,
                onAccountClick = onAccountClick
            )

            AccountLimitsSyncCard(
                lastSynced = settings?.lastSynced ?: 0L,
                neverValue = stringResource(R.string.common_never)
            )
        }
    }
}
