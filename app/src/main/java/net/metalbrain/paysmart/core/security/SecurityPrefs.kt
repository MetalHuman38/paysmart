package net.metalbrain.paysmart.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SecurityPrefs {
    private const val PREF_NAME = "security_prefs"
    private const val KEY_LAST_UNLOCK = "last_unlock"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun clear() {
        prefs.edit { clear() }
    }


    private lateinit var prefs: SharedPreferences

    var lastUnlockTimestamp: Long
        get() = prefs.getLong(KEY_LAST_UNLOCK, 0L)
        set(value) = prefs.edit { putLong(KEY_LAST_UNLOCK, value) }

    var lockAfterMinutes: Int
        get() = prefs.getInt("lock_after_minutes", 5)
        set(value) = prefs.edit { putInt("lock_after_minutes", value) }

    var passcodeEnabled: Boolean
        get() = prefs.getBoolean("passcode_enabled", false)
        set(value) = prefs.edit { putBoolean("passcode_enabled", value) }


    var createPasswordRequired: Boolean
        get() = prefs.getBoolean("create_password_required", true)
        set(value) = prefs.edit { putBoolean("create_password_required", value) }

}
