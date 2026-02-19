package net.metalbrain.paysmart.data.repository

interface PasswordRepository {
    suspend fun setPassword(plain: String, idToken: String)
    suspend fun verify(plain: String): Boolean

    suspend fun hasPassword(): Boolean

    suspend fun changePassword(old: String, new: String): Boolean
    suspend fun clear()
}
