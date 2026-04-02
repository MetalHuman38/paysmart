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
import net.metalbrain.paysmart.core.features.invoicing.routing.InvoiceSetupRoute
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceDetailViewModel
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupViewModel

internal fun NavGraphBuilder.invoiceNavGraph(
    navController: NavHostController
) {
    navigation(
        route = Screen.InvoiceFlow.route,
        startDestination = Screen.InvoiceWeeklyEntry.route
    ) {
        composable(Screen.InvoiceWeeklyEntry.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.InvoiceFlow.route)
            }
            val viewModel: InvoiceSetupViewModel = hiltViewModel(parentEntry)
            InvoiceSetupRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenInvoice = { invoiceId ->
                    navController.navigateInGraph(Screen.InvoiceDetail.routeWithInvoiceId(invoiceId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.InvoiceWorkerProfile.route) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.InvoiceFlow.route)
            }
            val viewModel: InvoiceSetupViewModel = hiltViewModel(parentEntry)
            InvoiceSetupRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenInvoice = { invoiceId ->
                    navController.navigateInGraph(Screen.InvoiceDetail.routeWithInvoiceId(invoiceId)) {
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
            InvoiceSetupRoute(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
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
