package net.metalbrain.paysmart.data.native

object NativeBridge {
    init {
        System.loadLibrary("native-lib")
    }
    @JvmStatic
    external fun deriveKeyFromCpp(
        password: String,
        salt: ByteArray,
        iterations: Int,
        keyLength: Int
    ): ByteArray
}
