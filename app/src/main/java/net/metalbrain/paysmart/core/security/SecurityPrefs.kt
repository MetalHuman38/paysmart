package net.metalbrain.paysmart.core.security

import android.content.Context
import android.content.SharedPreferences

object SecurityPrefs {
    private const val PREF_NAME = "security_prefs"
    private const val KEY_LAST_UNLOCK = "last_unlock"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }


    private lateinit var prefs: SharedPreferences

    var lastUnlockTimestamp: Long
        get() = prefs.getLong(KEY_LAST_UNLOCK, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_UNLOCK, value).apply()
}
