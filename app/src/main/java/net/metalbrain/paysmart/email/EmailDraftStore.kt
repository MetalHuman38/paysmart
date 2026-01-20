package net.metalbrain.paysmart.email

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "email_draft")

@Singleton
class EmailDraftStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val store = context.dataStore

    val draft: Flow<EmailDraft> = store.data.map { prefs ->
        EmailDraft(
            email = prefs[EmailPrefsKeys.email],
            verified = prefs[EmailPrefsKeys.verified] ?: false
        )
    }

    suspend fun saveDraft(draft: EmailDraft) {
        store.edit { prefs ->
            draft.email?.let { prefs[EmailPrefsKeys.email] = it }
            prefs[EmailPrefsKeys.verified] = draft.verified
        }
    }

    suspend fun clear() {
        store.edit { it.clear() }
    }
}
