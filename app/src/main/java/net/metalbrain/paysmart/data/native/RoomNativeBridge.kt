package net.metalbrain.paysmart.data.native

object RoomNativeBridge {
    init {
        System.loadLibrary("room-lib")
    }

    @JvmStatic
    external fun encryptString(plain: String, key: String): String

    @JvmStatic
    external fun decryptString(encrypted: String, key: String): String

    @JvmStatic
    external fun deriveRoomKey(
        passphrase: String,
        salt: ByteArray,
        iterations: Int = 150_000,
        keyLength: Int = 32
    ): ByteArray
}
