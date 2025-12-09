package net.metalbrain.paysmart.phone

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PhonePrefsKeys {
    val e164 = stringPreferencesKey("e164")
    val verificationId = stringPreferencesKey("verification_id")
    val verified = booleanPreferencesKey("verified")
}
