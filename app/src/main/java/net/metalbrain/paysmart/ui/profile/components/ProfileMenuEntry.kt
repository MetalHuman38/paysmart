package net.metalbrain.paysmart.ui.profile.components

import androidx.compose.ui.graphics.vector.ImageVector

data class ProfileMenuEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

