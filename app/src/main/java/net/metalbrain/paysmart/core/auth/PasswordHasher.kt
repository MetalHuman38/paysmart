package net.metalbrain.paysmart.core.auth

import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordHasher {

    companion object {
        private const val BCRYPT_COST = 12
        private const val EXPECTED_VERSION = $$"$2y$"
    }

    fun hash(plain: String): String {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plain.toCharArray())
    }

    fun verify(plain: String, hashed: String): Boolean {
        return BCrypt.verifyer().verify(plain.toCharArray(), hashed).verified
    }

    fun needsRehash(hashed: String): Boolean {
        // Check if the stored hash uses different cost or version
        val cost = extractCost(hashed)
        val version = hashed.take(4) // "$2y$"

        return cost != BCRYPT_COST || version != EXPECTED_VERSION
    }

    private fun extractCost(hashed: String): Int {
        return hashed.split('$')
            .getOrNull(2)
            ?.toIntOrNull()
            ?: -1
    }
}
