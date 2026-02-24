package net.metalbrain.paysmart.domain

import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    fun getStartupLanguage(): String
    fun getSelectedLanguage(): Flow<String>
    suspend fun setSelectedLanguage(languageCode: String)
}
