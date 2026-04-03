package net.metalbrain.paysmart.ui.home.data

import androidx.compose.ui.graphics.vector.ImageVector

data class HomeQuickAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
