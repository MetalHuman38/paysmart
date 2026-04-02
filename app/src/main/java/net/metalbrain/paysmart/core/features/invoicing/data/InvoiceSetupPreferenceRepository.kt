package net.metalbrain.paysmart.core.features.invoicing.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class InvoiceSetupSelection(
    val professionId: String? = null,
    val templateId: String? = null
)

@Singleton
class InvoiceSetupPreferenceRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val LAST_PROFESSION_ID = stringPreferencesKey("invoice_last_profession_id")
        val LAST_TEMPLATE_ID = stringPreferencesKey("invoice_last_template_id")
    }

    fun observeSelection(): Flow<InvoiceSetupSelection> {
        return dataStore.data.map { prefs ->
            InvoiceSetupSelection(
                professionId = prefs[LAST_PROFESSION_ID],
                templateId = prefs[LAST_TEMPLATE_ID]
            )
        }
    }

    suspend fun setSelection(
        professionId: String?,
        templateId: String?
    ) {
        dataStore.edit { prefs ->
            if (professionId.isNullOrBlank()) {
                prefs.remove(LAST_PROFESSION_ID)
            } else {
                prefs[LAST_PROFESSION_ID] = professionId
            }

            if (templateId.isNullOrBlank()) {
                prefs.remove(LAST_TEMPLATE_ID)
            } else {
                prefs[LAST_TEMPLATE_ID] = templateId
            }
        }
    }
}
