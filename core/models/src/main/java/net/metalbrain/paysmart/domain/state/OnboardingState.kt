package net.metalbrain.paysmart.domain.state

data class OnboardingState(
    val required: Map<String, Boolean> = emptyMap(),
    val completed: Map<String, Boolean> = emptyMap()
) {
    fun nextPendingStep(): String? {
        return required.entries
            .filter { it.value }
            .firstOrNull { (step, _) -> completed[step] != true }
            ?.key
    }

    fun isComplete(): Boolean = nextPendingStep() == null
}
