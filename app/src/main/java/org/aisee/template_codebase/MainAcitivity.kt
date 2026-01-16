package org.aisee.template_codebase

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.aisee.template_codebase.camera.AnalysisHandler
import org.aisee.template_codebase.camera.CameraCore
import org.aisee.template_codebase.camera.PhotoHandler
import org.aisee.template_codebase.utils.AccessibilityHelper.Companion.enableAccessibilityService
import org.aisee.template_codebase.utils.AccessibilityHelper.Companion.isAccessibilityServiceEnabled

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enable Button Service
        enableAccessibilityService(this)

        if (isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
            Log.d(TAG, "Accessibility service is enabled.")
        } else {
            Log.d(TAG, "Accessibility service not enabled even after auto-enable attempt.")
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
