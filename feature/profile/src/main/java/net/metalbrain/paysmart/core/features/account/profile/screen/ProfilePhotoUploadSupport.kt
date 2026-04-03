package net.metalbrain.paysmart.core.features.account.profile.screen

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

private const val ProfilePhotoMaxDimension = 1200
private const val ProfilePhotoQuality = 88

class PreparedProfilePhoto(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val previewModel: Any
)

fun prepareProfilePhoto(context: Context, uri: Uri): PreparedProfilePhoto {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val maxDimension = max(info.size.width, info.size.height).coerceAtLeast(1)
        if (maxDimension > ProfilePhotoMaxDimension) {
            val scale = ProfilePhotoMaxDimension.toFloat() / maxDimension.toFloat()
            decoder.setTargetSize(
                (info.size.width * scale).toInt().coerceAtLeast(1),
                (info.size.height * scale).toInt().coerceAtLeast(1)
            )
        }
    }
    val output = ByteArrayOutputStream()
    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, ProfilePhotoQuality, output)
    bitmap.recycle()
    val encodedBytes = output.toByteArray()
    return PreparedProfilePhoto(
        fileName = resolveProfilePhotoDisplayName(context, uri),
        mimeType = "image/jpeg",
        bytes = encodedBytes,
        previewModel = encodedBytes
    )
}

fun createProfilePhotoCaptureUri(context: Context): Uri {
    val directory = File(context.cacheDir, "profile-photo").apply { mkdirs() }
    val file = File(directory, "captured-avatar.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
}

private fun resolveProfilePhotoDisplayName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            cursor.getString(index)?.takeIf { it.isNotBlank() }?.let { return it }
        }
    }
    return "profile-photo.jpg"
}
