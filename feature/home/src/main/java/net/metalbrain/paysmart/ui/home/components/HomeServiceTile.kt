package net.metalbrain.paysmart.ui.home.components

import androidx.compose.ui.graphics.vector.ImageVector

data class HomeServiceTile(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
