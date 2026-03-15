package net.metalbrain.paysmart.core.features.capabilities.catalog

data class CapabilityItem(
    val key: CapabilityKey,
    val title: String,
    val subtitle: String,
    val footnote: String? = null
)
