package net.metalbrain.paysmart.utils

private val HEX_CHARS = "0123456789abcdef".toCharArray()

fun ByteArray.toHexString(): String {
    val out = CharArray(size * 2)
    var i = 0
    forEach { b ->
        val v = b.toInt() and 0xFF
        out[i++] = HEX_CHARS[v ushr 4]
        out[i++] = HEX_CHARS[v and 0x0F]
    }
    return String(out)
}
