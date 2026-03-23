package net.metalbrain.paysmart.navigator

import android.net.Uri
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceDetailRoute
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceVenueSetupRoute
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceWorkerProfileRoute
import net.metalbrain.paysmart.core.features.invoicing.utils.InvoiceWeeklyEntryRoute
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceDetailViewModel
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupViewModel

internal fun NavGraphBuilder.invoiceNavGraph(
    navController: NavHostController
) {
    navigation(
        route = Screen.InvoiceFlow.route,
        startDestination = Screen.InvoiceWorkerProfile.route
    ) {
        composable(Screen.InvoiceWorkerProfile.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.InvoiceFlow.route)
            }
            val viewModel: InvoiceSetupViewModel = hiltViewModel(parentEntry)
            InvoiceWorkerProfileRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onContinue = {
                    navController.navigateInGraph(Screen.InvoiceVenueSetup.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.InvoiceVenueSetup.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.InvoiceFlow.route)
            }
            val viewModel: InvoiceSetupViewModel = hiltViewModel(parentEntry)
            InvoiceVenueSetupRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRequireProfile = {
                    navController.navigateInGraph(Screen.InvoiceWorkerProfile.route) {
                        launchSingleTop = true
                    }
                },
                onContinue = {
                    navController.navigateInGraph(Screen.InvoiceWeeklyEntry.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.InvoiceWeeklyEntry.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.InvoiceFlow.route)
            }
            val viewModel: InvoiceSetupViewModel = hiltViewModel(parentEntry)
            InvoiceWeeklyEntryRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRequireProfileSetup = {
                    navController.navigateInGraph(Screen.InvoiceWorkerProfile.route) {
                        launchSingleTop = true
                    }
                },
                onRequireVenueSetup = {
                    navController.navigateInGraph(Screen.InvoiceVenueSetup.route) {
                        launchSingleTop = true
                    }
                },
                onOpenInvoice = { invoiceId ->
                    navController.navigateInGraph(Screen.InvoiceDetail.routeWithInvoiceId(invoiceId)) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }

    composable(
        route = Screen.InvoiceDetail.route,
        arguments = listOf(
            navArgument("invoiceId") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val viewModel: InvoiceDetailViewModel = hiltViewModel()
        val invoiceId = Uri.decode(backStackEntry.arguments?.getString("invoiceId").orEmpty())
        InvoiceDetailRoute(
            invoiceId = invoiceId,
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
        )
    }
}
