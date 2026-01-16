package org.aisee.template_codebase.camera

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * PhotoHandler: The Photographer.
 *
 * This class is responsible ONLY for configuring the high-quality photo stream
 * and performing the actual file saving when a photo is requested.
 */
class PhotoHandler(private val context: Context) {

    private var imageCapture: ImageCapture? = null

    /**
     * Creates and returns the ImageCapture UseCase.
     * This tells the camera: "Prepare a high-res buffer for photos."
     */
    fun getImageCapture(): ImageCapture {
        if (imageCapture == null) {
            imageCapture = ImageCapture.Builder()
                .build()
        }
        return imageCapture!!
    }

    /**
     * Triggers the photo capture.
     */
    fun takePhoto() {
        val capture = imageCapture
        if (capture == null) {
            Log.e(TAG, "ImageCapture use case is null. Is the camera bound?")
            return
        }

        // 1. Create file location in public gallery
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val photoFile = File(
            storageDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        // 2. Prepare options
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // 3. Snap picture
        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo capture succeeded: ${photoFile.absolutePath}")
                    // Scan so it appears in Gallery
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(photoFile.absolutePath),
                        arrayOf("image/jpeg"),
                        null
                    )
                }
            }
        )
    }

    /**
     * Triggers the photo capture with a custom callback.
     */
    fun takePhoto(callback: ImageCapture.OnImageSavedCallback) {
        val capture = imageCapture
        if (capture == null) {
            Log.e(TAG, "ImageCapture use case is null. Is the camera bound?")
            return
        }

        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val photoFile = File(
            storageDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions =
            ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo capture succeeded: ${photoFile.absolutePath}")

                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(photoFile.absolutePath),
                        arrayOf("image/jpeg"),
                        null
                    )

                    // Forward result to caller
                    callback.onImageSaved(output)
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)

                    // Forward error to caller
                    callback.onError(exc)
                }
            }
        )
    }
    companion object {
        private const val TAG = "PhotoHandler"
    }
}
