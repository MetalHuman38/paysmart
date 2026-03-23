package net.metalbrain.paysmart.navigator

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.metalbrain.paysmart.core.features.addmoney.screen.AddMoneyScreen
import net.metalbrain.paysmart.core.features.featuregate.FeatureKey
import net.metalbrain.paysmart.core.features.fundingaccount.screen.FundingAccountRoute
import net.metalbrain.paysmart.core.features.fundingaccount.viewmodel.FundingAccountViewModel
import net.metalbrain.paysmart.core.features.sendmoney.screen.SendMoneyRecipientScreen
import net.metalbrain.paysmart.ui.home.screen.BalanceDetailsRoute
import net.metalbrain.paysmart.ui.home.viewmodel.BalanceDetailsViewModel

internal fun NavGraphBuilder.walletNavGraph(
    navController: NavHostController
) {
    composable(Screen.AddMoney.route) {
        AddMoneyScreen(
            onBack = { navController.popBackStack() },
            onOpenAccountDetails = { currencyCode ->
                navController.navigateInGraph(
                    Screen.BalanceDetails.routeWithCurrency(
                        currencyCode = currencyCode,
                        tab = Screen.BalanceDetails.Tab.ACCOUNT_DETAILS
                    )
                )
            }
        )
    }

    composable(Screen.FundingAccount.route) {
        val viewModel: FundingAccountViewModel = hiltViewModel()
        FundingAccountRoute(
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
    }

    composable(
        route = Screen.UkAccount.route,
        arguments = listOf(
            navArgument(Screen.UkAccount.CURRENCYARG) {
                type = NavType.StringType
                defaultValue = "GBP"
            }
        )
    ) {
        val viewModel: BalanceDetailsViewModel = hiltViewModel()
        BalanceDetailsRoute(
            viewModel = viewModel,
            initialTab = Screen.BalanceDetails.Tab.ACCOUNT_DETAILS,
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
                navController.navigateInGraph(
                    Screen.TransactionDetail.routeWithTransactionId(transaction.id)
                )
            }
        )
    }

    composable(Screen.SendMoney.route) {
        SendMoneyRecipientScreen(
            onBack = { navController.popBackStack() }
        )
    }
}
