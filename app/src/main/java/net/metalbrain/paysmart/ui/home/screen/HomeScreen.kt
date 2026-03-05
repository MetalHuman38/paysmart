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
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.core.features.featuregate.FeatureKey
import net.metalbrain.paysmart.core.features.capabilities.catalog.CountryCapabilityCatalog
import net.metalbrain.paysmart.core.features.account.security.viewmodel.SecurityViewModel
import net.metalbrain.paysmart.ui.home.components.HomeContent
import net.metalbrain.paysmart.ui.home.nav.HomeBottomNavigation
import net.metalbrain.paysmart.ui.home.viewmodel.HomeViewModel
import java.util.Locale

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
        val securityViewModel = hiltViewModel<SecurityViewModel>()
        val uiState by homeViewModel.uiState.collectAsState()
        val isBalanceHidden by securityViewModel.hideBalanceEnabled.collectAsState()
        val balances = uiState.balanceSnapshot.balancesByCurrency
        val primaryCurrency = resolvePrimaryCurrency(
            balancesByCurrency = balances,
            preferredCurrency = uiState.balanceSnapshot.preferredCurrencyCode
        )
        val primaryAmount = balances[primaryCurrency] ?: 0.0

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
                onSendMoneyClick = {
                    navController.navigate(
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.SEND_MONEY.id,
                            resumeRoute = Screen.SendMoney.route
                        )
                    )
                },
                onBalanceCardClick = {
                    navController.navigate(
                        Screen.BalanceDetails.routeWithArgs(primaryCurrency, primaryAmount)
                    )
                },
                onRewardCardClick = {
                    navController.navigate(
                        Screen.RewardDetails.routeWithPoints(uiState.rewardEarned.points)
                    )
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
                        Screen.FeatureGate.routeWithArgs(
                            feature = FeatureKey.SEND_MONEY.id,
                            resumeRoute = Screen.SendMoney.route
                        )
                    )
                },
                onViewAllLimitsClick = {
                    navController.navigate(Screen.ProfileAccountLimits.route)
                },
                localSettings = uiState.security,
                transactions = uiState.recentTransactions,
                balanceSnapshot = uiState.balanceSnapshot,
                rewardEarned = uiState.rewardEarned,
                countryFlagEmoji = uiState.countryFlagEmoji,
                countryCurrencyCode = uiState.countryCurrencyCode,
                capabilities = uiState.capabilities,
                exchangeRateSnapshot = uiState.exchangeRateSnapshot,
                isBalanceVisible = !isBalanceHidden,
                onToggleBalanceVisibility = {
                    securityViewModel.setHideBalance(!isBalanceHidden)
                }
            )
        }
    }
}

private fun resolvePrimaryCurrency(
    balancesByCurrency: Map<String, Double>,
    preferredCurrency: String
): String {
    val normalizedPreferred = preferredCurrency.trim().uppercase(Locale.US)
    if (normalizedPreferred.isNotBlank() && balancesByCurrency.containsKey(normalizedPreferred)) {
        return normalizedPreferred
    }

    if (balancesByCurrency.isEmpty()) {
        return normalizedPreferred.ifBlank {
            CountryCapabilityCatalog.defaultProfile().currencyCode
        }
    }
    return balancesByCurrency.keys.minOf { it.uppercase(Locale.US) }
}
