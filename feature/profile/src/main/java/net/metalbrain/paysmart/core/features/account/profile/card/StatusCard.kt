package net.metalbrain.paysmart.core.features.account.profile.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.core.features.account.profile.data.colors.ProfileCardTone

@Composable
fun StatusCard(
    title: String,
    description: String,
    tone: ProfileCardTone,
    modifier: Modifier = Modifier
) {
    ProfileCard(
        title = title,
        description = description,
        tone = tone,
        modifier = modifier
    )
}
