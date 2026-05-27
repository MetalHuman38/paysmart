package net.metalbrain.paysmart.core.features.invoicing.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import net.metalbrain.paysmart.core.features.invoicing.utils.PROGRESS_VISIBLE_STEPS
import net.metalbrain.paysmart.core.features.invoicing.utils.progressForStep
import net.metalbrain.paysmart.core.features.invoicing.viewmodel.InvoiceSetupUiState
import net.metalbrain.paysmart.core.invoice.model.InvoiceFormStep
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.PaysmartTheme


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
    val colors = PaysmartTheme.colorTokens
    val typography = PaysmartTheme.typographyTokens

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
                style = typography.labelMedium,
                color = colors.textSecondary
            )
            state.selectedProfession?.let { profession ->
                Text(
                    text = profession.name,
                    modifier = Modifier.weight(1f),
                    style = typography.labelMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = title,
            style = typography.heading3,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = body,
            style = typography.bodySmall,
            color = colors.textSecondary
        )
        Surface(
            color = colors.surfacePrimary,
            shape = PaysmartTheme.radius.pill
        ) {
            LinearProgressIndicator(
                progress = { progressForStep(state.formStep) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.xs)
                    .padding(horizontal = Dimens.xs),
                color = colors.brandPrimary,
                trackColor = colors.fillDisabled
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
