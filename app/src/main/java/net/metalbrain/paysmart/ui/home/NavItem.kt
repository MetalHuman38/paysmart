package net.metalbrain.paysmart.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavItem(val label: String, val icon: ImageVector, val route: String) {
    object Home : NavItem("Home", Icons.Filled.Home, "home")
    object Transactions : NavItem("Transactions", Icons.Filled.SwapHoriz, "transactions")
    object Referral : NavItem("Referral", Icons.Filled.CardGiftcard, "referral")
    object Help : NavItem("Help", Icons.AutoMirrored.Filled.HelpOutline, "help")
}
