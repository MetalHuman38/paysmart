package net.metalbrain.paysmart.ui.home.screen

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
        val uiState by homeViewModel.uiState.collectAsState()
        val balances = uiState.balanceSnapshot.balancesByCurrency
        val primaryCurrency = resolvePrimaryCurrency(balances)
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
                onSecurityClick = {
                    navController.navigate(Screen.Reauthenticate.baseRoute)
                },
                onLinkAccountClick = {
                    navController.navigate(Screen.LinkFederatedAccount.route)
                },
                onVerifyEmailClick = {
                    navController.navigate(Screen.AddEmail.route)
                },
                onAddAddressClick = {
                    navController.navigate(Screen.ProfileScreen.route)
                },
                onVerifyIdentityClick = {
                    navController.navigate(Screen.ProfileScreen.route)
                },
                localSettings = uiState.security,
                transactions = uiState.recentTransactions,
                balanceSnapshot = uiState.balanceSnapshot,
                rewardEarned = uiState.rewardEarned
            )
        }
    }
}

private fun resolvePrimaryCurrency(balancesByCurrency: Map<String, Double>): String {
    if (balancesByCurrency.isEmpty()) {
        return "GBP"
    }
    if (balancesByCurrency.containsKey("GBP")) {
        return "GBP"
    }
    return balancesByCurrency.keys.minOf { it.uppercase(Locale.US) }
}
