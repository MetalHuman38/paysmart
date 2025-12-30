package net.metalbrain.paysmart.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.metalbrain.paysmart.domain.LanguageRepository

class LanguageRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<androidx.datastore.preferences.core.Preferences>
) : LanguageRepository {

    companion object {
        val LANGUAGE_KEY = stringPreferencesKey("language_pref")
    }

    override fun getSelectedLanguage(): Flow<String> =
        dataStore.data.map { prefs ->
            prefs[LANGUAGE_KEY] ?: "en"
        }

    override suspend fun setSelectedLanguage(languageCode: String) {
        dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = languageCode
        }
    }
}
