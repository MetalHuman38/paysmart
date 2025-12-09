package net.metalbrain.paysmart.phone

import android.content.Context
import androidx.datastore.core.DataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "phone_draft")

@Singleton
class PhoneDraftStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = context.dataStore

    val draft: Flow<PhoneDraft> = store.data.map { prefs: Preferences ->
        PhoneDraft(
            e164 = prefs[PhonePrefsKeys.e164],
            verificationId = prefs[PhonePrefsKeys.verificationId],
            verified = prefs[PhonePrefsKeys.verified] ?: false
        )
    }

    suspend fun saveDraft(draft: PhoneDraft) {
        store.edit { prefs ->
            if (draft.e164 != null) {
                prefs[PhonePrefsKeys.e164] = draft.e164
            }
            if (draft.verificationId != null) {
                prefs[PhonePrefsKeys.verificationId] = draft.verificationId
            }
            prefs[PhonePrefsKeys.verified] = draft.verified
        }
    }

    suspend fun clear() {
        store.edit { it.clear() }
    }
}
