package net.metalbrain.paysmart.core.features.theme.data

enum class AppThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    fun next(): AppThemeMode {
        return when (this) {
            SYSTEM -> LIGHT
            LIGHT -> DARK
            DARK -> SYSTEM
        }
    }

    companion object {
        fun fromStorage(raw: String?): AppThemeMode {
            return entries.firstOrNull { it.storageValue == raw } ?: SYSTEM
        }
    }
}
