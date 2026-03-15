package net.metalbrain.paysmart.core.features.featuregate

data class SecurityStrengthScore(
    val methods: Set<SecurityStrengthMethod>
) {
    val level: Int
        get() = methods.size

    fun meets(minimumLevel: Int): Boolean = level >= minimumLevel
}
