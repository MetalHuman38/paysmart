package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.compose.ui.graphics.vector.ImageVector

data class ProfileMenuEntry(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
