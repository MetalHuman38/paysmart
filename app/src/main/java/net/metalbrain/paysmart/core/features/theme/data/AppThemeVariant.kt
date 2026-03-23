package net.metalbrain.paysmart.core.features.theme.data

enum class AppThemeVariant(val storageValue: String) {
    PAYSMART("paysmart"),
    OBSIDIAN("obsidian");

    fun next(): AppThemeVariant {
        return when (this) {
            PAYSMART -> OBSIDIAN
            OBSIDIAN -> PAYSMART
        }
    }

    companion object {
        fun fromStorage(raw: String?): AppThemeVariant {
            return entries.firstOrNull { it.storageValue == raw } ?: PAYSMART
        }
    }
}
