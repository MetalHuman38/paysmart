package net.metalbrain.paysmart.core.features.invoicing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.core.features.invoicing.utils.PROGRESS_VISIBLE_STEPS
import net.metalbrain.paysmart.core.features.invoicing.utils.progressForStep
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.core.invoice.model.InvoiceFormStep
import net.metalbrain.paysmart.ui.theme.Dimens


@Composable
fun InvoiceSetupProgressHeader(
    state: InvoiceSetupUiState,
    currentStepIndex: Int,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    onSelectTemplate: (String) -> Unit
) {
    val displayedStep = if (state.formStep == InvoiceFormStep.QUICK_START) {
        1
    } else {
        currentStepIndex + 1
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Dimens.xs),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (state.formStep == InvoiceFormStep.QUICK_START) {
                    "Quick start"
                } else {
                    "Step $displayedStep of ${PROGRESS_VISIBLE_STEPS.size}"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            state.selectedProfession?.let { profession ->
                Text(
                    text = profession.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.large
        ) {
            LinearProgressIndicator(
                progress = { progressForStep(state.formStep) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.xs)
                    .padding(horizontal = Dimens.xs),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        }
        if (state.availableTemplates.size > 1) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Dimens.sm)
            ) {
                state.availableTemplates.forEach { template ->
                    FilterChip(
                        selected = state.selectedTemplate?.id == template.id,
                        onClick = { onSelectTemplate(template.id) },
                        label = {
                            Text(
                                text = template.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    }
}
