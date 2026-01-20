package net.metalbrain.paysmart.core.auth

import jakarta.inject.Inject
import net.metalbrain.paysmart.data.native.NativePasswordBridge

class NativePasswordHasher @Inject constructor() : BcryptPasswordHasher {

    override fun hash(plain: String): String {
        return NativePasswordBridge.hashPassword(plain)
    }

    override fun verify(plain: String, hashed: String): Boolean {
        return NativePasswordBridge.verifyPassword(plain, hashed)
    }

    override fun needsRehash(hashed: String): Boolean {
        // Optional: logic to rehash based on version/format
        return false
    }
}
