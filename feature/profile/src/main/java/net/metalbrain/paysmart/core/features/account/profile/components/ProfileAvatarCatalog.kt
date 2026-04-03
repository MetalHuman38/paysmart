package net.metalbrain.paysmart.core.features.account.profile.components

import androidx.annotation.DrawableRes
import net.metalbrain.paysmart.feature.profile.R

data class ProfileAvatarPreset(
    val token: String,
    @param:DrawableRes val drawableResId: Int
)

object ProfileAvatarCatalog {
    val presets = listOf(
        ProfileAvatarPreset("preset:avatar", R.drawable.avatar),
        ProfileAvatarPreset("preset:avatar1", R.drawable.avatar1),
        ProfileAvatarPreset("preset:avatar2", R.drawable.avatar2)
    )

    fun presetForPhotoUrl(photoUrl: String?): ProfileAvatarPreset? {
        return presets.firstOrNull { it.token == photoUrl }
    }
}
