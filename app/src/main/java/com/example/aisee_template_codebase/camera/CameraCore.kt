package com.example.aisee_template_codebase.camera

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraCore: The Engine.
 * 
 * This class is responsible for the low-level CameraX management.
 * It does NOT know about photos or ML. It only knows how to:
 * 1. Connect to the physical camera hardware.
 * 2. Manage the Lifecycle (Start/Stop).
 * 3. Bind specific "Use Cases" (given to it by other classes) to the camera.
 */
class CameraCore(private val context: Context) : LifecycleOwner {

    private val lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    
    // Holds use cases that need to be bound once the provider is ready
    private var pendingUseCases: List<UseCase>? = null

    init {
        // We set the lifecycle to STARTED so the camera thinks the app is open and active.
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        initializeProvider()
    }

    private fun initializeProvider() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider
                
                // If there were use cases waiting to be bound, bind them now
                val pending = pendingUseCases
                if (pending != null) {
                    Log.d(TAG, "Camera provider ready. Binding ${pending.size} pending use cases.")
                    bind(pending)
                    pendingUseCases = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize camera provider", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Binds a list of UseCases (Photo, Video, ML, etc.) to the camera lifecycle.
     * This effectively turns them "ON".
     */
    fun bind(useCases: List<UseCase>) {
        val provider = cameraProvider
        if (provider == null) {
            // Provider not ready yet. Queue these use cases.
            Log.d(TAG, "Camera provider not ready. Queuing ${useCases.size} use cases for later binding.")
            pendingUseCases = useCases
            return
        }

        ContextCompat.getMainExecutor(context).execute {
            try {
                // Critical: Unbind everything first to avoid collisions or invalid states
                provider.unbindAll()

                if (useCases.isNotEmpty()) {
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    // The '*' operator spreads the list into separate arguments.
                    // This is where Parallel Execution happens.
                    provider.bindToLifecycle(
                        this, 
                        cameraSelector, 
                        *useCases.toTypedArray()
                    )
                    Log.d(TAG, "Camera bound with ${useCases.size} use cases.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to bind use cases", e)
            }
        }
    }

    fun getExecutor(): ExecutorService = cameraExecutor

    fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        cameraExecutor.shutdown()
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    companion object {
        private const val TAG = "CameraCore"
    }
}
