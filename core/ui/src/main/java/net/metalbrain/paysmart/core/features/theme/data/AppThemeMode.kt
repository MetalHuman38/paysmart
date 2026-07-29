package net.metalbrain.paysmart.core.features.theme.data

enum class AppThemeMode(val storageValue: String) {
    LIGHT("light"),
    DARK("dark");

    fun next(): AppThemeMode {
        return when (this) {
            LIGHT -> DARK
            DARK -> LIGHT
        }
    }

    companion object {
        fun fromStorage(raw: String?): AppThemeMode {
            return entries.firstOrNull { it.storageValue == raw } ?: DARK
        }
    }
}
