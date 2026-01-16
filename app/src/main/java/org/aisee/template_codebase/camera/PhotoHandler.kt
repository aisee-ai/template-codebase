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
import androidx.camera.core.ImageProxy

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
    /**
     * Captures an image in memory (no file), forwarding the ImageProxy to the caller.
     * Caller MUST close the ImageProxy (image.close()).
     */
    fun takePhoto(callback: ImageCapture.OnImageCapturedCallback) {
        val capture = imageCapture
        if (capture == null) {
            Log.e(TAG, "ImageCapture use case is null. Is the camera bound?")
            return
        }

        capture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {

                override fun onCaptureSuccess(image: ImageProxy) {
                    try {
                        callback.onCaptureSuccess(image)
                    } catch (t: Throwable) {
                        Log.e(TAG, "Error in onCaptureSuccess callback: ${t.message}", t)
                        image.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    callback.onError(exception)
                }
            }
        )
    }

    companion object {
        private const val TAG = "PhotoHandler"
    }
}
