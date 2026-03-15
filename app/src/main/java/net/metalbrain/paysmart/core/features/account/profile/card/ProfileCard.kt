package net.metalbrain.paysmart.core.features.account.profile.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.metalbrain.paysmart.core.features.account.profile.data.colors.ProfileCardTone
import net.metalbrain.paysmart.core.features.account.profile.util.profileCardToneColors
import net.metalbrain.paysmart.ui.theme.Dimens

@Composable
fun ProfileCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    tone: ProfileCardTone = ProfileCardTone.Neutral,
    supportingItems: List<String> = emptyList(),
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = profileCardToneColors(tone)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = colors.containerColor,
            contentColor = colors.contentColor
        ),
        border = BorderStroke(1.dp, colors.borderColor)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.supportingColor
            )

            if (supportingItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.xs)) {
                    supportingItems.forEach { item ->
                        Text(
                            text = "\u2022 $item",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.contentColor
                        )
                    }
                }
            }

            if (actionText != null && onAction != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.xs),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(horizontal = Dimens.md, vertical = 14.dp)
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
