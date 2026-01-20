package net.metalbrain.paysmart.domain.crypto

import jakarta.inject.Inject
import net.metalbrain.paysmart.data.native.NativeBridge

class NativeCryptoUseCase @Inject constructor() : CryptoUseCase {
    override fun deriveKey(
        password: String,
        salt: ByteArray,
        iterations: Int,
        keyLength: Int
    ): ByteArray {
        return NativeBridge.deriveKeyFromCpp(password, salt, iterations, keyLength)
    }
}
