package org.aisee.template_codebase

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import org.aisee.template_codebase.camera.AnalysisHandler
import org.aisee.template_codebase.camera.CameraCore
import org.aisee.template_codebase.camera.PhotoHandler
import org.aisee.template_codebase.ml.MlKitProcessor

@SuppressLint("AccessibilityPolicy")
class MyAccessibilityService : AccessibilityService() {
    
    // Modular Camera System
    private var cameraCore: CameraCore? = null
    private var photoHandler: PhotoHandler? = null
    private var analysisHandler: AnalysisHandler? = null
    
    private var mlKitProcessor: MlKitProcessor? = null

    override fun onServiceConnected() {
        Log.d(TAG, "Accessibility Service Connected")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyCodeName = KeyEvent.keyCodeToString(event.keyCode)
            Log.d(TAG, "Physical Button Pressed: $keyCodeName")
            
            if (event.keyCode == KeyEvent.KEYCODE_F2) {
                Log.d(TAG, "F2 Pressed: Taking Photo...")

                photoHandler?.takePhoto()
                return true
            }
        }
        return false
    }
    
    private fun setupMlKit() {
        try {
            mlKitProcessor = MlKitProcessor(this)
            mlKitProcessor?.start() // Starts the overlay
            
            if (mlKitProcessor != null) {
                // 1. Create the Analysis Use Cases
                val analysisUseCase = analysisHandler?.createAnalysis(mlKitProcessor!!)
                
                // 2. Create the Preview (UI) Use Case
                val surfaceProvider = mlKitProcessor?.getSurfaceProvider()
                var previewUseCase: Preview? = null
                if (surfaceProvider != null) {
                    previewUseCase = analysisHandler?.createPreview(surfaceProvider)
                }

                // 3. Bind EVERYTHING together (Photo + Analysis + Preview)
                val allUseCases = mutableListOf<UseCase>()
                
                // Always include Photo Capture
                allUseCases.add(photoHandler!!.getImageCapture())
                
                if (analysisUseCase != null) allUseCases.add(analysisUseCase)
                if (previewUseCase != null) allUseCases.add(previewUseCase)

                // Re-bind to apply the new parallel configuration
                cameraCore?.bind(allUseCases)
            }
            Log.d(TAG, "ML Kit initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup ML Kit", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Use this to intercept UI events
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Accessibility Service Unbound")
        
        mlKitProcessor?.stop()
        analysisHandler?.shutdown()
        cameraCore?.onDestroy()
        
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }

    companion object {
        private const val TAG = "MyAccessibilityService"
    }
}
