package net.metalbrain.paysmart.room.converters

import net.metalbrain.paysmart.data.native.RoomNativeBridge


class EncryptedStringConverter(private val key: String) {
    fun encrypt(value: String?): String =
        value?.let { RoomNativeBridge.encryptString(it, key) } ?: ""

    fun decrypt(value: String?): String =
        value?.let { RoomNativeBridge.decryptString(it, key) } ?: ""
}
