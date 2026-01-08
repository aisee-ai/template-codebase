package com.example.aisee_template_codebase

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainAcitivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Attempt to auto-enable first
        enableAccessibilityService(this)

        if (isAccessibilityServiceEnabled(this, MyAccessibilityService::class.java)) {
            Log.d(TAG, "Accessibility service is enabled.")
        } else {
            Log.d(TAG, "Accessibility service not enabled even after auto-enable attempt.")
        }
    }


    //Helper method to enable accessibility service
    private fun enableAccessibilityService(context: Context) {
        val serviceName = "${context.packageName}/${MyAccessibilityService::class.java.name}"

        val cmd1 = "settings put secure enabled_accessibility_services $serviceName"
        ShellOperator.runCommand(cmd1)

        val cmd2 = "settings put secure accessibility_enabled 1"
        ShellOperator.runCommand(cmd2)

        Log.d(TAG, "Attempted to enable accessibility service automatically.")
    }

    //Helper method to check if accessibility service is enabled
    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expectedComponentName = ComponentName(context, service)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledComponent = ComponentName.unflattenFromString(componentNameString)
            if (enabledComponent != null && enabledComponent == expectedComponentName) {
                return true
            }
        }
        return false
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
