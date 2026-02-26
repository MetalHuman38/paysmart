package net.metalbrain.paysmart.core.features.language.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.core.locale.LocaleManager
import net.metalbrain.paysmart.domain.LanguageRepository

class LanguageRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) : LanguageRepository {

    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language_pref")
    }

    override fun getStartupLanguage(): String = LocaleManager.getSavedLanguage(context)

    override fun getSelectedLanguage(): Flow<String> =
        dataStore.data.map { prefs ->
            prefs[LANGUAGE_KEY] ?: getStartupLanguage()
        }

    override suspend fun setSelectedLanguage(languageCode: String) {
        val normalized = languageCode.ifBlank { getStartupLanguage() }
        // Keep boot locale cache in sync with DataStore for process restarts.
        LocaleManager.saveLanguage(context, normalized)
        dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = normalized
        }
    }
}
