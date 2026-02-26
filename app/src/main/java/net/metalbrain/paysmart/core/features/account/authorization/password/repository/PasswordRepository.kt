package net.metalbrain.paysmart.core.features.account.authorization.password.repository

interface PasswordRepository {
    suspend fun setPassword(plain: String, idToken: String)
    suspend fun verify(plain: String): Boolean

    suspend fun hasPassword(): Boolean

    suspend fun changePassword(old: String, new: String, idToken: String): Boolean
    suspend fun clear()
}
