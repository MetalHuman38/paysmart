package net.metalbrain.paysmart.core.features.account.profile.util

import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.core.features.account.profile.data.colors.ProfileCardTone
import net.metalbrain.paysmart.core.features.account.profile.data.colors.ProfileCardToneColors
import net.metalbrain.paysmart.ui.theme.PaysmartTheme


@Composable
fun profileCardToneColors(tone: ProfileCardTone): ProfileCardToneColors {
    val colors = PaysmartTheme.colorTokens
    return when (tone) {
        ProfileCardTone.Neutral -> ProfileCardToneColors(
            containerColor = colors.surfaceElevated,
            contentColor = colors.textPrimary,
            supportingColor = colors.textSecondary,
            borderColor = colors.borderSubtle
        )

        ProfileCardTone.Warning -> ProfileCardToneColors(
            containerColor = colors.warning.copy(alpha = 0.12f),
            contentColor = colors.textPrimary,
            supportingColor = colors.textSecondary,
            borderColor = colors.warning.copy(alpha = 0.28f)
        )

        ProfileCardTone.Positive -> ProfileCardToneColors(
            containerColor = colors.success.copy(alpha = 0.12f),
            contentColor = colors.textPrimary,
            supportingColor = colors.textSecondary,
            borderColor = colors.success.copy(alpha = 0.28f)
        )

        ProfileCardTone.Critical -> ProfileCardToneColors(
            containerColor = colors.error.copy(alpha = 0.12f),
            contentColor = colors.textPrimary,
            supportingColor = colors.textSecondary,
            borderColor = colors.error.copy(alpha = 0.28f)
        )
    }
}
