package net.metalbrain.paysmart.core.features.account.authentication.email.data

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

/**
 * A persistent data store for managing an email address draft and its verification status.
 *
 * This class uses Android DataStore Preferences to store [EmailDraft] objects, allowing
 * the application to preserve the user's progress during email-related authentication flows.
 *
 * @property context The application context used to initialize the underlying DataStore.
 */
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
