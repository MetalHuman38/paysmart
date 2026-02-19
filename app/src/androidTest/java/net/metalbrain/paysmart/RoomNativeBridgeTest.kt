// RoomNativeBridgeTest.kt
package net.metalbrain.paysmart

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import jakarta.inject.Inject
import net.metalbrain.paysmart.data.native.RoomNativeBridge
import net.metalbrain.paysmart.di.AppModule
import net.metalbrain.paysmart.utils.RoomKeyProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(AppModule::class, AppModule.PhoneModule::class)
class RoomNativeBridgeTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var keyProvider: RoomKeyProvider

    companion object {
        @JvmStatic
        @BeforeClass
        fun loadLib() {
            // Use Instrumentation context for safety
            System.loadLibrary("room-lib")
        }
    }

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testEncryptDecryptString() {
        val plain = "MySuperSecretValue"

        // 32 bytes (256-bit) HEX key
        val keyHex = keyProvider.getKeyHex()

        val encrypted = RoomNativeBridge.encryptString(plain, keyHex)
        val decrypted = RoomNativeBridge.decryptString(encrypted, keyHex)

        assertEquals(plain, decrypted)
    }

    @Test
    fun testDeriveRoomKey128bit() {
        val pass = "test-passphrase"
        val salt = ByteArray(16) { it.toByte() }
        val keyLen = 16
        val iterations = 10000

        val key = RoomNativeBridge.deriveRoomKey(pass, salt, iterations, keyLen)
        assertEquals(keyLen, key.size)
    }

    @Test
    fun testDeriveRoomKey256bit() {
        val pass = "test-passphrase"
        val salt = ByteArray(16) { (it * 2).toByte() }
        val keyLen = 32
        val iterations = 20000

        val key = RoomNativeBridge.deriveRoomKey(pass, salt, iterations, keyLen)
        assertEquals(keyLen, key.size)
    }

    @Test
    fun testRejectInvalidKeySize() {
        val pass = "invalid-key"
        val salt = ByteArray(16) { 0 }
        val keyLen = 24
        val iterations = 1000

        try {
            RoomNativeBridge.deriveRoomKey(pass, salt, iterations, keyLen)
            fail("Expected IllegalArgumentException for unsupported key length") // 👈 clearer failure
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }
}
