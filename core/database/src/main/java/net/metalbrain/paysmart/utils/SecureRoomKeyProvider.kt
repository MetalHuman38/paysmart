package net.metalbrain.paysmart.utils

class SecureRoomKeyProvider : RoomKeyProvider {
    override fun getKeyHex(): String {
        // This should fetch securely from keystore or your RoomPassphraseRepository
        throw IllegalStateException("Not implemented in prod yet")
    }
}
