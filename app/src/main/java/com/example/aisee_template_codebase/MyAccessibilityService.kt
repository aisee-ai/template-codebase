package com.example.aisee_template_codebase

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.aisee_template_codebase.ml.MlKitProcessor
import com.example.voice_activation_uart.VoiceActivation

class MyAccessibilityService : AccessibilityService() {

    private var voiceActivation: VoiceActivation? = null
    private var cameraCapture: CameraCapture? = null
    private var mlKitProcessor: MlKitProcessor? = null

    // Do NOT CHANGE THIS
    private val VAD_DEVICE_PATH = "/dev/ttyS0"
    private val VAD_BAUD_RATE = 9600

    override fun onServiceConnected() {
        Log.d(TAG, "Accessibility Service Connected")
        
        // Initialize Camera
        cameraCapture = CameraCapture(this)

        // Initialize Voice Activity Detection
        setupVoiceActivation()

        // Initialize ML Kit - COMMENT THIS LINE TO DISABLE ML & OVERLAY
//        setupMlKit()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyCodeName = KeyEvent.keyCodeToString(event.keyCode)
            Log.d(TAG, "Physical Button Pressed: $keyCodeName")
            
            if (event.keyCode == KeyEvent.KEYCODE_F2) {
                Log.d(TAG, "F2 Pressed: Taking Photo...")
                // Custom logic here, we have used taking a photo as an example
                cameraCapture?.takePhoto()
                return true
            }
        }
        return false
    }
    
    private fun setupMlKit() {
        try {
            mlKitProcessor = MlKitProcessor(this)
            mlKitProcessor?.start() // Starts the overlay
            
            // Connect the ML processor and its SurfaceProvider (for video) to the camera stream
            if (mlKitProcessor != null) {
                cameraCapture?.setAnalyzer(
                    mlKitProcessor!!,
                    mlKitProcessor?.getSurfaceProvider()
                )
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
                            // Add your custom logic here
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
        cameraCapture?.onDestroy()
        
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
