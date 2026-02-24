package net.metalbrain.paysmart.ui.profile.components

import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.File
import java.util.concurrent.Executor


fun captureIdentityDocument(
    imageCapture: ImageCapture,
    cacheDir: File,
    executor: Executor,
    onSuccess: (fileName: String, mimeType: String, bytes: ByteArray) -> Unit,
    onFailure: (String) -> Unit
) {
    val outputFile = runCatching {
        File.createTempFile("identity_capture_", ".jpg", cacheDir)
    }.getOrElse { error ->
        onFailure(error.localizedMessage ?: "Unable to prepare capture file")
        return
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) {
                outputFile.delete()
                onFailure(exception.message ?: "Camera capture failed")
            }

            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                runCatching {
                    val bytes = outputFile.readBytes()
                    require(bytes.isNotEmpty()) { "Captured image is empty" }
                    onSuccess(
                        outputFile.name,
                        "image/jpeg",
                        bytes
                    )
                }.onFailure { error ->
                    onFailure(error.localizedMessage ?: "Unable to process captured image")
                }
                outputFile.delete()
            }
        }
    )
}
