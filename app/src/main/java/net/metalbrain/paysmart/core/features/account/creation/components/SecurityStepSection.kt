package net.metalbrain.paysmart.core.features.account.creation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import net.metalbrain.paysmart.core.features.account.creation.card.PostOtpSecurityStepCard
import net.metalbrain.paysmart.core.features.account.creation.data.SecurityStepSpec
import net.metalbrain.paysmart.ui.theme.Dimens
import androidx.compose.ui.unit.dp

@Composable
fun SecurityStepSection(
    title: String,
    steps: List<SecurityStepSpec>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            steps.forEachIndexed { index, step ->
                PostOtpSecurityStepCard(step = step)

                if (index != steps.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                    )
                }
            }
        }
    }
}
