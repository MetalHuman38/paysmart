package net.metalbrain.paysmart.core.features.invoicing.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceCardTone
import net.metalbrain.paysmart.core.features.invoicing.screen.InvoiceSurfaceCard
import net.metalbrain.paysmart.core.features.invoicing.utils.professionIcon
import net.metalbrain.paysmart.core.invoice.model.Profession
import net.metalbrain.paysmart.ui.theme.Dimens


@Composable
fun InvoiceProfessionSelectionCard(
    professions: List<Profession>,
    selectedProfessionId: String?,
    onSelectProfession: (Profession) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.md)) {
        professions.forEach { profession ->
            InvoiceSurfaceCard(
                tone = if (selectedProfessionId == profession.id) {
                    InvoiceCardTone.Accent
                } else {
                    InvoiceCardTone.Default
                },
                onClick = { onSelectProfession(profession) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(Dimens.heightX / 2),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = professionIcon(profession.icon),
                                contentDescription = profession.name,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                    ) {
                        Text(
                            text = profession.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        profession.description?.takeIf { it.isNotBlank() }?.let { description ->
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
