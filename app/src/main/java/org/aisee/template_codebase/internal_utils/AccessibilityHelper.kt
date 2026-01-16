package org.aisee.template_codebase.internal_utils

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import org.aisee.template_codebase.MyAccessibilityService

class AccessibilityHelper {

    companion object {

        // Helper method to enable accessibility service
        @JvmStatic
        fun enableAccessibilityService(context: Context) {
            val serviceName = "${context.packageName}/${MyAccessibilityService::class.java.name}"

            val cmd1 = "settings put secure enabled_accessibility_services $serviceName"
            ShellOperator.runCommand(cmd1)

            val cmd2 = "settings put secure accessibility_enabled 1"
            ShellOperator.runCommand(cmd2)

            Log.d(TAG, "Attempted to enable accessibility service automatically.")
        }

        // Helper method to check if accessibility service is enabled
        @JvmStatic
        fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
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

        private const val TAG = "AccessibilityHelper"
    }
}