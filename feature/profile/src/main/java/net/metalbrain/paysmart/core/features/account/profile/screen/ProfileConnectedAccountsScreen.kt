package net.metalbrain.paysmart.core.features.account.profile.screen

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.metalbrain.paysmart.core.features.account.profile.card.ConnectedAccountsOverviewCard
import net.metalbrain.paysmart.core.features.account.profile.components.BankAccountsTabContent
import net.metalbrain.paysmart.core.features.account.profile.components.CardsTabContent
import net.metalbrain.paysmart.core.features.account.profile.components.ConnectedAccountsTab
import net.metalbrain.paysmart.core.features.account.profile.components.ConnectedAccountsTabSwitcher
import net.metalbrain.paysmart.core.features.cards.state.ManagedCardsUiState
import net.metalbrain.paysmart.core.features.fundingaccount.state.FundingAccountUiState
import net.metalbrain.paysmart.feature.profile.R
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileConnectedAccountsScreen(
    fundingAccountState: FundingAccountUiState,
    managedCardsState: ManagedCardsUiState,
    onBack: () -> Unit,
    onRefreshBankAccounts: () -> Unit,
    onProvisionBankAccount: () -> Unit,
    onCopyBankAccountNumber: () -> Unit,
    onShareBankAccount: () -> Unit,
    onRefreshCards: () -> Unit,
    onSetDefaultCard: (String) -> Unit,
    onRemoveCard: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(ConnectedAccountsTab.BANK_ACCOUNTS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.profile_menu_connected_accounts_title),
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = Dimens.screenPadding,
                top = Dimens.md,
                end = Dimens.screenPadding,
                bottom = Dimens.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg)
        ) {

            item {
                ConnectedAccountsOverviewCard()
            }

            item {
                ConnectedAccountsTabSwitcher(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }

            item {
                Crossfade(
                    targetState = selectedTab,
                    label = "connected_accounts_tab"
                ) { tab: ConnectedAccountsTab ->
                    when (tab) {
                        ConnectedAccountsTab.BANK_ACCOUNTS -> BankAccountsTabContent(
                            state = fundingAccountState,
                            onRefresh = onRefreshBankAccounts,
                            onProvision = onProvisionBankAccount,
                            onCopyAccountNumber = onCopyBankAccountNumber,
                            onShareDetails = onShareBankAccount
                        )

                        ConnectedAccountsTab.CARDS -> CardsTabContent(
                            state = managedCardsState,
                            onRefresh = onRefreshCards,
                            onSetDefaultCard = onSetDefaultCard,
                            onRemoveCard = onRemoveCard
                        )
                    }
                }
            }
        }
    }
}
