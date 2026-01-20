package net.metalbrain.paysmart.data.native

object NativePasswordBridge {
    init {
        System.loadLibrary("native-lib")
    }

    @JvmStatic
    external fun hashPassword(password: String): String

    @JvmStatic
    external fun verifyPassword(password: String, storedHash: String): Boolean
}
