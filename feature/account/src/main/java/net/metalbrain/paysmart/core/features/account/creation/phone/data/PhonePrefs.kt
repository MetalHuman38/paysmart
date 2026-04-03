package net.metalbrain.paysmart.core.features.account.creation.phone.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PhonePrefsKeys {
    val e164 = stringPreferencesKey("e164")
    val verificationId = stringPreferencesKey("verification_id")
    val verified = booleanPreferencesKey("verified")
    val errorMessage = stringPreferencesKey("error_message")
}
