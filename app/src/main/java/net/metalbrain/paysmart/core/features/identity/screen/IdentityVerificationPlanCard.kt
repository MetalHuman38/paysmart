package net.metalbrain.paysmart.core.features.identity.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.core.features.account.profile.state.resolveStatus
import net.metalbrain.paysmart.core.features.identity.state.IdentitySetupResolverUiState
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityResolverStep
import net.metalbrain.paysmart.ui.theme.Dimens
import net.metalbrain.paysmart.ui.theme.HomeCardTokens

@Composable
fun IdentityVerificationPlanCard(state: IdentitySetupResolverUiState) {
    val completedSteps = resolverPlanSteps.count { step ->
        state.resolveStatus(step) == IdentityStepStatus.COMPLETED
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = HomeCardTokens.cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = HomeCardTokens.defaultElevation)
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.xs)
                ) {
                    Text(
                        text = stringResource(R.string.identity_resolver_plan_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = state.planHeadline(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.82f)
                ) {
                    Text(
                        text = "$completedSteps/${resolverPlanSteps.size}",
                        modifier = Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            LinearProgressIndicator(
                progress = { state.planProgress() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.sm)
                    .clip(CircleShape),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = when {
                    state.failedStep != null -> MaterialTheme.colorScheme.error
                    state.isProcessing -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.secondary
                }
            )

            Column(verticalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                resolverPlanSteps.forEach { step ->
                    IdentityStepLine(
                        label = stringResource(step.labelRes()),
                        status = state.resolveStatus(step)
                    )
                }
            }
        }
    }
}

private val resolverPlanSteps = listOf(
    IdentityResolverStep.CAPTURE,
    IdentityResolverStep.ENCRYPT,
    IdentityResolverStep.UPLOAD,
    IdentityResolverStep.ATTEST,
    IdentityResolverStep.COMMIT
)

@Composable
private fun IdentitySetupResolverUiState.planHeadline(): String {
    return when {
        currentStep == IdentityResolverStep.COMPLETE -> {
            stringResource(R.string.identity_resolver_completed_message)
        }
        isValidatingCapture -> {
            stringResource(R.string.identity_resolver_capture_validating)
        }
        isProcessing -> {
            stringResource(currentStep.labelRes())
        }
        hasCapturedDocument -> {
            stringResource(R.string.identity_resolver_submit_action)
        }
        else -> {
            stringResource(R.string.identity_resolver_capture_document_action)
        }
    }
}

private fun IdentitySetupResolverUiState.planProgress(): Float {
    val completedCount = resolverPlanSteps.count { step ->
        resolveStatus(step) == IdentityStepStatus.COMPLETED
    }.toFloat()
    val totalSteps = resolverPlanSteps.size.toFloat()

    return when {
        currentStep == IdentityResolverStep.COMPLETE -> 1f
        isValidatingCapture -> 0.12f
        isProcessing -> ((completedCount + 0.45f) / totalSteps).coerceIn(0f, 1f)
        else -> (completedCount / totalSteps).coerceIn(0f, 1f)
    }
}

private fun IdentityResolverStep.labelRes(): Int {
    return when (this) {
        IdentityResolverStep.CAPTURE -> R.string.identity_resolver_capture_step
        IdentityResolverStep.ENCRYPT -> R.string.identity_resolver_encrypt_step
        IdentityResolverStep.UPLOAD -> R.string.identity_resolver_upload_step
        IdentityResolverStep.ATTEST -> R.string.identity_resolver_attest_step
        IdentityResolverStep.COMMIT -> R.string.identity_resolver_commit_step
        IdentityResolverStep.COMPLETE -> R.string.identity_resolver_completed_message
    }
}
