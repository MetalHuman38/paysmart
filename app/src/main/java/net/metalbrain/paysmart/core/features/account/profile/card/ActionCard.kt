package net.metalbrain.paysmart.core.features.account.profile.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.metalbrain.paysmart.core.features.account.profile.data.colors.ProfileCardTone

@Composable
fun ActionCard(
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    tone: ProfileCardTone = ProfileCardTone.Neutral,
    supportingItems: List<String> = emptyList()
) {
    ProfileCard(
        title = title,
        description = description,
        tone = tone,
        modifier = modifier,
        supportingItems = supportingItems,
        actionText = actionText,
        onAction = onAction
    )
}
