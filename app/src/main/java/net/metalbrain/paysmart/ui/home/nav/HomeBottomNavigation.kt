package net.metalbrain.paysmart.ui.home.nav

import android.util.Log
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import net.metalbrain.paysmart.ui.Screen
import net.metalbrain.paysmart.ui.home.nav.NavItem

@Composable
fun HomeBottomNavigation(
    navController: NavHostController
) {
    val tag = "HomeBottomNav"
    val items = listOf(
        NavItem.Home,
        NavItem.Transactions,
        NavItem.Referral,
        NavItem.Help
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()

    NavigationBar {
        items.forEach { item ->
            val selected = navBackStackEntry
                ?.destination
                ?.hierarchy
                ?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (navController.graph.findNode(item.route) == null) {
                        Log.w(tag, "Route not found in graph: ${item.route}")
                        return@NavigationBarItem
                    }

                    if (item.route == Screen.Home.route) {
                        val poppedToHome = navController.popBackStack(Screen.Home.route, false)
                        if (!poppedToHome) {
                            navController.navigate(Screen.Home.route) {
                                launchSingleTop = true
                            }
                        }
                        return@NavigationBarItem
                    }

                    navController.navigate(item.route) {
                        popUpTo(Screen.Home.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
