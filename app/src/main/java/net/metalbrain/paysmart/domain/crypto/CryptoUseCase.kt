package net.metalbrain.paysmart.domain.crypto

interface CryptoUseCase {
    fun deriveKey(
        password: String,
        salt: ByteArray,
        iterations: Int = 150_000,
        keyLength: Int = 32
    ): ByteArray
}
