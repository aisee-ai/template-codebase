package org.aisee.template_codebase.ml

import android.content.Context
import android.util.Log
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import org.aisee.template_codebase.camera.AnalysisHandler
import org.aisee.template_codebase.camera.CameraCore
import org.aisee.template_codebase.camera.PhotoHandler

class MlKitTrigger {
    private var mlKitProcessor: MlKitProcessor? = null

    private var cameraCore: CameraCore? = null
    private var photoHandler: PhotoHandler? = null
    private var analysisHandler: AnalysisHandler? = null

    private fun setupMlKit(context: Context) {
        try {
            mlKitProcessor = MlKitProcessor(context)
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

    companion object {
        private const val TAG = "MLKitTrigger"
    }
}
