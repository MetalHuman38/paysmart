package net.metalbrain.paysmart.core.features.account.profile.state

import net.metalbrain.paysmart.core.features.identity.screen.IdentityStepStatus
import net.metalbrain.paysmart.core.features.identity.state.IdentitySetupResolverUiState
import net.metalbrain.paysmart.core.features.identity.viewmodel.IdentityResolverStep

fun IdentitySetupResolverUiState.resolveStatus(
    step: IdentityResolverStep
): IdentityStepStatus {
    if (failedStep == step) {
        return IdentityStepStatus.FAILED
    }

    if (currentStep == IdentityResolverStep.COMPLETE) {
        return IdentityStepStatus.COMPLETED
    }

    if (step == IdentityResolverStep.CAPTURE) {
        return if (hasCapturedDocument || isProcessing) {
            IdentityStepStatus.COMPLETED
        } else {
            IdentityStepStatus.IN_PROGRESS
        }
    }

    return when {
        currentStep.ordinal > step.ordinal -> IdentityStepStatus.COMPLETED
        currentStep == step && isProcessing -> IdentityStepStatus.IN_PROGRESS
        else -> IdentityStepStatus.PENDING
    }
}
