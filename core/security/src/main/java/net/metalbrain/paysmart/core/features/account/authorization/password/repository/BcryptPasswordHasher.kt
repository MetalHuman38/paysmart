package net.metalbrain.paysmart.core.features.account.authorization.password.repository

interface BcryptPasswordHasher {
    fun hash(plain: String): String
    fun verify(plain: String, hashed: String): Boolean
    fun needsRehash(hashed: String): Boolean
}
