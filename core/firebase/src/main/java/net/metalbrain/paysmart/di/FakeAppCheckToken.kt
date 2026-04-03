package net.metalbrain.paysmart.di

import com.google.firebase.appcheck.AppCheckToken

class FakeAppCheckToken(private val tokenValue: String) : AppCheckToken() {
    override fun getToken(): String = tokenValue
    override fun getExpireTimeMillis(): Long = System.currentTimeMillis() + 60_000
}
