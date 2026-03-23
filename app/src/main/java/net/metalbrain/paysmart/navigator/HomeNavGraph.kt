package net.metalbrain.paysmart.navigator

import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.metalbrain.paysmart.core.features.featuregate.FeatureKey
import net.metalbrain.paysmart.core.features.help.screen.HelpScreen
import net.metalbrain.paysmart.core.features.help.viewmodel.HelpViewModel
import net.metalbrain.paysmart.core.features.notifications.screen.NotificationCenterScreen
import net.metalbrain.paysmart.core.features.notifications.viewmodel.NotificationCenterViewModel
import net.metalbrain.paysmart.core.features.referral.screen.ReferralScreen
import net.metalbrain.paysmart.core.features.referral.viewmodel.ReferralViewModel
import net.metalbrain.paysmart.core.features.transactions.screen.TransactionDetailRoute
import net.metalbrain.paysmart.core.features.transactions.screen.TransactionsScreen
import net.metalbrain.paysmart.core.features.transactions.viewmodel.TransactionDetailViewModel
import net.metalbrain.paysmart.core.features.fx.screen.ExchangeRatesScreen
import net.metalbrain.paysmart.core.features.fx.viewmodel.ExchangeRatesViewModel
import net.metalbrain.paysmart.ui.home.screen.BalanceDetailsRoute
import net.metalbrain.paysmart.ui.home.screen.HomeScreen
import net.metalbrain.paysmart.ui.home.screen.RewardDetailsRoute
import net.metalbrain.paysmart.ui.home.viewmodel.BalanceDetailsViewModel
import net.metalbrain.paysmart.ui.home.viewmodel.RewardDetailsViewModel

internal fun NavGraphBuilder.homeNavGraph(
    navController: NavHostController
) {
    composable(Screen.Home.route) {
        HomeScreen(
            navController = navController,
        )
    }

    composable(Screen.NotificationCenter.route) {
        val viewModel: NotificationCenterViewModel = hiltViewModel()
        NotificationCenterScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.BalanceDetails.route,
        arguments = listOf(
            navArgument(Screen.BalanceDetails.CURRENCYARG) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(Screen.BalanceDetails.TAB_ARG) {
                type = NavType.StringType
                defaultValue = Screen.BalanceDetails.Tab.TRANSACTIONS.routeValue
            }
        )
    ) { backStackEntry ->
        val viewModel: BalanceDetailsViewModel = hiltViewModel()
        val initialTab = Screen.BalanceDetails.Tab.fromRouteValue(
            backStackEntry.arguments?.getString(Screen.BalanceDetails.TAB_ARG)
        )
        BalanceDetailsRoute(
            viewModel = viewModel,
            initialTab = initialTab,
            onBack = { navController.popBackStack() },
            onViewAccountLimitsClick = { currencyCode ->
                navController.navigateInGraph(
                    Screen.ProfileAccountLimitsDetails.routeWithCurrency(currencyCode)
                ) {
                    launchSingleTop = true
                }
            },
            onSendClick = {
                navController.navigateInGraph(
                    Screen.FeatureGate.routeWithArgs(
                        feature = FeatureKey.SEND_MONEY.id,
                        resumeRoute = Screen.SendMoney.route
                    )
                )
            },
            onAddClick = {
                navController.navigateInGraph(
                    Screen.FeatureGate.routeWithArgs(
                        feature = FeatureKey.ADD_MONEY.id,
                        resumeRoute = Screen.AddMoney.route
                    )
                )
            },
            onWithdrawClick = {
                navController.navigateInGraph(Screen.Help.route) {
                    launchSingleTop = true
                }
            },
            onConvertClick = {
                navController.navigateInGraph(Screen.SendMoney.route) {
                    launchSingleTop = true
                }
            },
            onTransactionClick = { transaction ->
                navController.navigateInGraph(Screen.TransactionDetail.routeWithTransactionId(transaction.id))
            }
        )
    }

    composable(
        route = Screen.ExchangeRates.route,
        arguments = listOf(
            navArgument(Screen.ExchangeRates.COUNTRY_ISO2_ARG) {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) {
        val viewModel: ExchangeRatesViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsState()
        ExchangeRatesScreen(
            state = state,
            onBack = { navController.popBackStack() },
            onRefresh = viewModel::refresh,
            onSendClick = {
                navController.navigateInGraph(
                    Screen.FeatureGate.routeWithArgs(
                        feature = FeatureKey.SEND_MONEY.id,
                        resumeRoute = Screen.SendMoney.route
                    )
                )
            }
        )
    }

    composable(Screen.RewardDetails.route) {
        val viewModel: RewardDetailsViewModel = hiltViewModel()
        RewardDetailsRoute(
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onHelpClick = {
                navController.navigateInGraph(Screen.Help.route) {
                    launchSingleTop = true
                }
            },
            onTransactionClick = { transaction ->
                navController.navigateInGraph(Screen.TransactionDetail.routeWithTransactionId(transaction.id))
            }
        )
    }

    composable(Screen.Transactions.route) {
        TransactionsScreen(
            navController = navController
        )
    }

    composable(
        route = Screen.TransactionDetail.route,
        arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
    ) { backStackEntry ->
        val transactionId = Uri.decode(backStackEntry.arguments?.getString("transactionId") ?: "")
        val transactionDetailViewModel: TransactionDetailViewModel = hiltViewModel()
        TransactionDetailRoute(
            transactionId = transactionId,
            viewModel = transactionDetailViewModel,
            onBack = { navController.popBackStack() }
        )
    }

    composable(Screen.Referral.route) {
        val referralViewModel: ReferralViewModel = hiltViewModel()
        ReferralScreen(
            navController = navController,
            viewModel = referralViewModel
        )
    }

    composable(Screen.Help.route) {
        val helpViewModel: HelpViewModel = hiltViewModel()
        HelpScreen(
            navController = navController,
            viewModel = helpViewModel
        )
    }
}
