package net.metalbrain.paysmart.core.features.account.profile.data.colors

import androidx.compose.ui.graphics.Color

enum class ProfileCardTone {
    Neutral,
    Warning,
    Positive,
    Critical
}

data class ProfileCardToneColors(
    val containerColor: Color,
    val contentColor: Color,
    val supportingColor: Color,
    val borderColor: Color
)
