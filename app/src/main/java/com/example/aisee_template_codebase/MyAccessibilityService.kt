package com.example.aisee_template_codebase

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.camera.core.UseCase
import com.example.aisee_template_codebase.camera.AnalysisHandler
import com.example.aisee_template_codebase.camera.CameraCore
import com.example.aisee_template_codebase.camera.PhotoHandler
import com.example.aisee_template_codebase.ml.MlKitProcessor
import com.example.voice_activation_uart.VoiceActivation

class MyAccessibilityService : AccessibilityService() {

    private var voiceActivation: VoiceActivation? = null
    
    // New Modular Camera System
    private var cameraCore: CameraCore? = null
    private var photoHandler: PhotoHandler? = null
    private var analysisHandler: AnalysisHandler? = null
    
    private var mlKitProcessor: MlKitProcessor? = null

    // Do NOT CHANGE THIS
    private val VAD_DEVICE_PATH = "/dev/ttyS0"
    private val VAD_BAUD_RATE = 9600

    override fun onServiceConnected() {
        Log.d(TAG, "Accessibility Service Connected")
        
        // 1. Initialize the Camera System
        cameraCore = CameraCore(this)
        photoHandler = PhotoHandler(this)
        analysisHandler = AnalysisHandler()

        // 2. Start with just Photo Capture (Default)
        // This makes the camera ready to take pictures immediately.
        val defaultUseCases = listOf(photoHandler!!.getImageCapture())
        cameraCore?.bind(defaultUseCases)

        // Initialize Voice Activity Detection
        // setupVoiceActivation()

        // Initialize ML Kit - COMMENT THIS LINE TO DISABLE ML & OVERLAY
        setupMlKit()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyCodeName = KeyEvent.keyCodeToString(event.keyCode)
            Log.d(TAG, "Physical Button Pressed: $keyCodeName")
            
            if (event.keyCode == KeyEvent.KEYCODE_F2) {
                Log.d(TAG, "F2 Pressed: Taking Photo...")
                // Use the dedicated PhotoHandler
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
                // 1. Create the Analysis Use Cases, the model is configured in mlKitProcessor's detector.
                val analysisUseCase = analysisHandler?.createAnalysis(mlKitProcessor!!)
                
                // 2. Create the Preview (UI) Use Case
                val surfaceProvider = mlKitProcessor?.getSurfaceProvider()
                var previewUseCase: androidx.camera.core.Preview? = null
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

    private fun setupVoiceActivation() {
        try {
            voiceActivation = VoiceActivation.create(VAD_DEVICE_PATH, VAD_BAUD_RATE)
            
            voiceActivation?.events { e ->
                when (e) {
                    is VoiceActivation.Event.Command -> {
                        Log.i(TAG, "cmdId=${e.cmdId}")
                        if (e.cmdId == CMD_HEY_I_SEE) {
                            Log.d(TAG, "Wake word detected!")
                        }
                    }
                    is VoiceActivation.Event.Error -> Log.e(TAG, "UART error: ${e.message}")
                    VoiceActivation.Event.Started -> Log.i(TAG, "VoiceActivation started")
                    VoiceActivation.Event.Stopped -> Log.i(TAG, "VoiceActivation stopped")
                }
            }

            voiceActivation?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start VAD", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Use this to intercept UI events
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "Accessibility Service Unbound")
        voiceActivation?.stop()
        
        mlKitProcessor?.stop()
        analysisHandler?.shutdown()
        cameraCore?.onDestroy()
        
        return super.onUnbind(intent)
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }

    companion object {
        private const val TAG = "MyAccessabilityService"
        private const val CMD_HEY_I_SEE = 2
    }
}
