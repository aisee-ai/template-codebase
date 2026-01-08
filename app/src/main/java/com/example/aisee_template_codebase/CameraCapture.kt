package com.example.aisee_template_codebase

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraCapture(private val context: Context) : LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var preview: Preview? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var currentAnalyzer: ImageAnalysis.Analyzer? = null
    private var currentSurfaceProvider: Preview.SurfaceProvider? = null

    init {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED 
        startCamera()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                bindUseCases(cameraProvider)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get camera provider", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindUseCases(cameraProvider: ProcessCameraProvider) {
        try {
            // Unbind all use cases before rebinding to avoid collisions or resource locks
            cameraProvider.unbindAll()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val useCases = mutableListOf<androidx.camera.core.UseCase>()

            // 1. ImageCapture - High quality for photos
            imageCapture = ImageCapture.Builder().build()
            useCases.add(imageCapture!!)

            // 2. ImageAnalysis (if configured) - Lower resolution for performance/ML
            if (currentAnalyzer != null) {
                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setTargetResolution(Size(640, 480)) // Use VGA for analysis to save resources
                    .build()
                
                imageAnalysis?.setAnalyzer(cameraExecutor, currentAnalyzer!!)
                useCases.add(imageAnalysis!!)
                Log.d(TAG, "Binding ImageAnalysis")
            }

            // 3. Preview (if configured) - Shows video on UI
            if (currentSurfaceProvider != null) {
                preview = Preview.Builder().build()
                preview?.setSurfaceProvider(currentSurfaceProvider!!)
                useCases.add(preview!!)
                Log.d(TAG, "Binding Preview")
            }

            // Bind use cases to camera - CameraX handles parallel execution
            cameraProvider.bindToLifecycle(
                this, cameraSelector, *useCases.toTypedArray()
            )
            
            Log.d(TAG, "Camera initialized and bound. Use cases: ${useCases.size}")

        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    fun setAnalyzer(analyzer: ImageAnalysis.Analyzer, surfaceProvider: Preview.SurfaceProvider? = null) {
        currentAnalyzer = analyzer
        currentSurfaceProvider = surfaceProvider
        // Re-bind to apply changes
        startCamera()
    }

    fun removeAnalyzer() {
        currentAnalyzer = null
        currentSurfaceProvider = null
        startCamera()
    }

    fun takePhoto() {
        val imageCapture = imageCapture
        if (imageCapture == null) {
            Log.e(TAG, "ImageCapture is not initialized yet. Please wait for camera binding.")
            return
        }

        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val photoFile = File(
            storageDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "Photo capture succeeded: ${photoFile.absolutePath}")
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

    fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        cameraExecutor.shutdown()
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    companion object {
        private const val TAG = "CameraCapture"
    }
}
