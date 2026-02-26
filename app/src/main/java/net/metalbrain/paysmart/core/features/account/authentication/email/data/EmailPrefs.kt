package net.metalbrain.paysmart.core.features.account.authentication.email.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object EmailPrefsKeys {
    val email = stringPreferencesKey("email")
    val verified = booleanPreferencesKey("verified")
}
