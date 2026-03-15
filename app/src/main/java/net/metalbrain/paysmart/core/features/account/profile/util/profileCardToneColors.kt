package net.metalbrain.paysmart.core.features.account.profile.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import net.metalbrain.paysmart.core.features.account.profile.data.colors.ProfileCardTone
import net.metalbrain.paysmart.core.features.account.profile.data.colors.ProfileCardToneColors


@Composable
fun profileCardToneColors(tone: ProfileCardTone): ProfileCardToneColors {
    val colorScheme = MaterialTheme.colorScheme
    return when (tone) {
        ProfileCardTone.Neutral -> ProfileCardToneColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.45f),
            contentColor = colorScheme.onSurface,
            supportingColor = colorScheme.onSurfaceVariant,
            borderColor = colorScheme.outline.copy(alpha = 0.12f)
        )

        ProfileCardTone.Warning -> ProfileCardToneColors(
            containerColor = colorScheme.secondaryContainer,
            contentColor = colorScheme.onSecondaryContainer,
            supportingColor = colorScheme.onSecondaryContainer.copy(alpha = 0.84f),
            borderColor = colorScheme.onSecondaryContainer.copy(alpha = 0.12f)
        )

        ProfileCardTone.Positive -> ProfileCardToneColors(
            containerColor = colorScheme.tertiaryContainer,
            contentColor = colorScheme.onTertiaryContainer,
            supportingColor = colorScheme.onTertiaryContainer.copy(alpha = 0.84f),
            borderColor = colorScheme.onTertiaryContainer.copy(alpha = 0.12f)
        )

        ProfileCardTone.Critical -> ProfileCardToneColors(
            containerColor = colorScheme.errorContainer,
            contentColor = colorScheme.onErrorContainer,
            supportingColor = colorScheme.onErrorContainer.copy(alpha = 0.84f),
            borderColor = colorScheme.onErrorContainer.copy(alpha = 0.12f)
        )
    }
}
