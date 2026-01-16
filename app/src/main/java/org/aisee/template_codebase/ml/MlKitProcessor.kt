package org.aisee.template_codebase.ml

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

class MlKitProcessor(private val context: Context) : ImageAnalysis.Analyzer {

    private val detector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
    )

    private var windowManager: WindowManager? = null
    private var containerLayout: FrameLayout? = null
    private var overlayView: OverlayView? = null
    private var previewView: PreviewView? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun start() {
        // Initialize Window Manager
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Create a FrameLayout container
        containerLayout = FrameLayout(context)
        
        // 1. Create PreviewView (Background)
        previewView = PreviewView(context)
        val previewParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        containerLayout?.addView(previewView, previewParams)

        // 2. Create OverlayView (Foreground)
        overlayView = OverlayView(context)
        val overlayParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        containerLayout?.addView(overlayView, overlayParams)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            // Use TYPE_ACCESSIBILITY_OVERLAY for AccessibilityServices
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START

        try {
            windowManager?.addView(containerLayout, params)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add overlay view. Permission might be missing.", e)
        }
    }
    
    fun getSurfaceProvider(): Preview.SurfaceProvider? {
        return previewView?.surfaceProvider
    }

    fun stop() {
        try {
            if (containerLayout != null) {
                windowManager?.removeView(containerLayout)
                containerLayout = null
                previewView = null
                overlayView = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove overlay view", e)
        }
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { detectedObjects ->
                    // Map coordinates
                    val boxes = mutableListOf<Rect>()
                    val labels = mutableListOf<String>()

                    // Use the container size or overlay size for scaling
                    val targetWidth = overlayView?.width ?: 0
                    val targetHeight = overlayView?.height ?: 0

                    if (targetWidth > 0 && targetHeight > 0) {
                        // Adjust image dimensions based on rotation
                        val imageWidth: Int
                        val imageHeight: Int
                        if (rotationDegrees == 90 || rotationDegrees == 270) {
                            imageWidth = mediaImage.height
                            imageHeight = mediaImage.width
                        } else {
                            imageWidth = mediaImage.width
                            imageHeight = mediaImage.height
                        }

                        val scaleX = targetWidth.toFloat() / imageWidth
                        val scaleY = targetHeight.toFloat() / imageHeight
                        
                        for (obj in detectedObjects) {
                            val rect = obj.boundingBox
                            val scaledRect = Rect(
                                (rect.left * scaleX).toInt(),
                                (rect.top * scaleY).toInt(),
                                (rect.right * scaleX).toInt(),
                                (rect.bottom * scaleY).toInt()
                            )
                            boxes.add(scaledRect)
                            
                            val label = obj.labels.firstOrNull()?.text ?: "Object"
                            labels.add(label)
                        }

                        mainHandler.post {
                            overlayView?.updateBoxes(boxes, labels)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Detection failed", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    companion object {
        private const val TAG = "MlKitProcessor"
    }
}
