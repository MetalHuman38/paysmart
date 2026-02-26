package net.metalbrain.paysmart.core.features.identity.screen

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

private const val MaxIdentityUploadBytes = 10 * 1024 * 1024

@Composable
fun rememberIdentityDocumentPicker(
    onCaptured: (fileName: String, mimeType: String, bytes: ByteArray) -> Unit,
    onError: (String) -> Unit
): ManagedActivityResultLauncher<String, Uri?> {
    val context = LocalContext.current
    return rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching { readPickedIdentityDocument(context, uri) }
            .onSuccess { payload ->
                onCaptured(payload.fileName, payload.mimeType, payload.bytes)
            }
            .onFailure { error ->
                onError(error.localizedMessage ?: "Unable to capture document")
            }
    }
}

fun formatIdentityDocumentBytes(size: Int): String {
    if (size <= 0) return "0 B"
    val kb = size / 1024.0
    return if (kb < 1024) {
        String.format(Locale.US, "%.2f KB", kb)
    } else {
        String.format(Locale.US, "%.2f MB", kb / 1024.0)
    }
}

private data class PickedIdentityDocument(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray
)

private fun readPickedIdentityDocument(
    context: Context,
    uri: Uri
): PickedIdentityDocument {
    val bytes = context.contentResolver.openInputStream(uri)?.use { stream ->
        stream.readBytes()
    } ?: throw IllegalStateException("Unable to read selected file")

    if (bytes.size > MaxIdentityUploadBytes) {
        throw IllegalStateException("Selected file is too large")
    }

    return PickedIdentityDocument(
        fileName = resolveDisplayName(context, uri),
        mimeType = context.contentResolver.getType(uri) ?: "image/jpeg",
        bytes = bytes
    )
}

private fun resolveDisplayName(context: Context, uri: Uri): String {
    val resolver = context.contentResolver
    resolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            val value = cursor.getString(index)
            if (!value.isNullOrBlank()) {
                return value
            }
        }
    }
    return "identity_document"
}
