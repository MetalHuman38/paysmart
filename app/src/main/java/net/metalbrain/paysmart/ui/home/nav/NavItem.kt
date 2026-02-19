package net.metalbrain.paysmart.ui.home.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import net.metalbrain.paysmart.ui.Screen

sealed class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    object Home : NavItem("Home", Icons.Filled.Home, Screen.Home.route)
    object Transactions : NavItem("Transactions", Icons.Filled.SwapHoriz, Screen.Transactions.route)
    object Referral : NavItem("Referral", Icons.Filled.CardGiftcard, Screen.Referral.route)
    object Help : NavItem("Help", Icons.AutoMirrored.Filled.HelpOutline, Screen.Help.route)
}
