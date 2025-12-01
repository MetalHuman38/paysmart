package net.metalbrain.paysmart.domain

import kotlinx.coroutines.flow.Flow

interface LanguageRepository {
    fun getSelectedLanguage(): Flow<String>
    suspend fun setSelectedLanguage(languageCode: String)
}
