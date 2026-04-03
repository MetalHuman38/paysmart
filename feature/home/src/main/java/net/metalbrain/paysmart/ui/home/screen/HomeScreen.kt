package net.metalbrain.paysmart.ui.home.screen

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import net.metalbrain.paysmart.core.features.featuregate.FeatureKey
import net.metalbrain.paysmart.domain.model.Transaction
import net.metalbrain.paysmart.navigator.Screen
import net.metalbrain.paysmart.ui.home.components.HomeContent
import net.metalbrain.paysmart.ui.home.nav.HomeBottomNavigation
import net.metalbrain.paysmart.ui.home.support.resolvePrimaryBalanceCurrency
import net.metalbrain.paysmart.ui.home.viewmodel.HomeViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            HomeBottomNavigation(navController = navController)
        }
    ) { innerPadding ->
        val homeViewModel = hiltViewModel<HomeViewModel>()
        val uiState by homeViewModel.uiState.collectAsState()
        val isBalanceHidden by homeViewModel.hideBalanceEnabled.collectAsState()

        Box(modifier = Modifier.padding(innerPadding)) {
            HomeContent(
                onProfileClick = {
                    navController.navigate(Screen.ProfileScreen.route)
                },
                onReferralClick = {
                    navController.navigate(Screen.Referral.route)
                },
                onTransactionsClick = {
                    navController.navigate(Screen.Transactions.route)
                },
                onTransactionClick = { transaction: Transaction ->
                    navController.navigate(Screen.TransactionDetail.routeWithTransactionId(transaction.id))
                },
                onCreateInvoiceClick = {
                    navController.navigate(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.CREATE_INVOICE.id,
                            resumeRoute = Screen.InvoiceFlow.route
                        )
                    )
                },
                onSendMoneyClick = {
                    navController.navigate(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.SEND_MONEY.id,
                            resumeRoute = Screen.SendMoney.route
                        )
                    )
                },
                onRecentRecipientClick = { recipient ->
                    navController.navigate(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.SEND_MONEY.id,
                            resumeRoute = Screen.SendMoney.routeWithRecipientKey(recipient.recipientKey)
                        )
                    )
                },
                onReceiveMoneyClick = {
                    val receiveMoneyRoute = if (uiState.countryIso2.equals("GB", ignoreCase = true)) {
                        Screen.UkAccount.routeWithCurrency(uiState.countryCurrencyCode)
                    } else {
                        Screen.FundingAccount.route
                    }
                    navController.navigate(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.RECEIVE_MONEY.id,
                            resumeRoute = receiveMoneyRoute
                        )
                    )
                },
                onBalanceCardClick = {
                    val balanceCurrencyCode = resolvePrimaryBalanceCurrency(
                        balancesByCurrency = uiState.balanceSnapshot.balancesByCurrency,
                        preferredCurrencyCode = uiState.balanceSnapshot.preferredCurrencyCode
                    )
                    navController.navigate(
                        Screen.BalanceDetails.routeWithCurrency(balanceCurrencyCode)
                    )
                },
                onRewardCardClick = {
                    navController.navigate(Screen.RewardDetails.route)
                },
                onAddMoneyClick = {
                    navController.navigate(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.ADD_MONEY.id,
                            resumeRoute = Screen.AddMoney.route
                        )
                    )
                },
                onVerifyEmailClick = {
                    navController.navigate(
                        "${Screen.AddEmail.route}?returnRoute=${Uri.encode(Screen.Home.route)}"
                    )
                },
                onAddAddressClick = {
                    navController.navigate(Screen.ProfileAddressResolver.route)
                },
                onVerifyIdentityClick = {
                    navController.navigate(Screen.ProfileIdentityResolver.route)
                },
                onViewRatesClick = {
                    navController.navigate(
                        Screen.ExchangeRates.routeWithCountry(uiState.countryIso2)
                    )
                },
                onViewAllLimitsClick = {
                    navController.navigate(Screen.ProfileAccountLimits.route)
                },
                onNotificationClick = {
                    navController.navigate(Screen.NotificationCenter.route)
                },
                localSettings = uiState.security,
                displayName = uiState.displayName,
                transactions = uiState.recentTransactions,
                recentRecipients = uiState.recentRecipients,
                transactionSearchQuery = uiState.transactionSearchQuery,
                isTransactionSearchActive = uiState.isTransactionSearchActive,
                availableTransactionProviders = uiState.availableTransactionProviders,
                selectedTransactionProviders = uiState.selectedTransactionProviders,
                notification = uiState.notification,
                balanceSnapshot = uiState.balanceSnapshot,
                rewardEarned = uiState.rewardEarned,
                countryIso2 = uiState.countryIso2,
                countryFlagEmoji = uiState.countryFlagEmoji,
                countryCurrencyCode = uiState.countryCurrencyCode,
                launchInterest = uiState.launchInterest,
                capabilities = uiState.capabilities,
                exchangeRateSnapshot = uiState.exchangeRateSnapshot,
                isBalanceVisible = !isBalanceHidden,
                onTransactionSearchQueryChange = homeViewModel::onTransactionSearchQueryChanged,
                onTransactionProviderToggle = homeViewModel::onTransactionProviderToggled,
                onToggleBalanceVisibility = homeViewModel::onToggleBalanceVisibility
            )
        }
    }
}
