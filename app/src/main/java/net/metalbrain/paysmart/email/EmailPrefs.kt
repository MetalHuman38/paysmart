package net.metalbrain.paysmart.email

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object EmailPrefsKeys {
    val email = stringPreferencesKey("email")
    val verified = booleanPreferencesKey("verified")
}
