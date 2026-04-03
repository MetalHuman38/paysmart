package net.metalbrain.paysmart.domain.crypto

/**
 * Defines the contract for cryptographic operations within the domain.
 */
interface CryptoUseCase {
    fun deriveKey(
        password: String,
        salt: ByteArray,
        iterations: Int = 150_000,
        keyLength: Int = 32
    ): ByteArray
}
