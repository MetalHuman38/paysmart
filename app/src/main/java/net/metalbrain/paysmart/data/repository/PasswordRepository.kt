package net.metalbrain.paysmart.data.repository

interface PasswordRepository {
    suspend fun setPassword(plain: String)
    suspend fun verify(plain: String): Boolean
    suspend fun clear()
}
