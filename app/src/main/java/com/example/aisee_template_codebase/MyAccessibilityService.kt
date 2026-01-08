package com.example.aisee_template_codebase

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class MyAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        Log.d(TAG, "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not logging UI events as requested, only physical buttons
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyCodeName = KeyEvent.keyCodeToString(event.keyCode)
            Log.d(TAG, "Physical Button Pressed: $keyCodeName")
        }
        
        // Return false so the system handles the button press (e.g. actually changing volume)
        // Return true if you want to consume the event (block it)
        return false
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }

    companion object {
        private const val TAG = "MyAccessibilityService"
    }
}
