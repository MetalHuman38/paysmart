package net.metalbrain.paysmart.core.features.account.profile.data

import androidx.compose.ui.graphics.vector.ImageVector

data class ProfileAboutActionItem(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
