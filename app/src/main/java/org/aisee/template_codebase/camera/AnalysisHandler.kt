package org.aisee.template_codebase.camera

import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import java.util.concurrent.Executors

/**
 * AnalysisHandler: The Observer.
 *
 * This class handles the continuous video streams:
 * 1. ImageAnalysis (for ML Kit) - configured to be low-res (VGA) and fast.
 * 2. Preview (for UI) - configured to show what the camera sees on screen.
 */
class AnalysisHandler {

    var analysisUseCase: ImageAnalysis? = null
        private set
    var previewUseCase: Preview? = null
        private set

    private val analysisExecutor = Executors.newSingleThreadExecutor()

    /**
     * Creates and returns the ImageAnalysis UseCase.
     */
    fun createAnalysis(analyzer: ImageAnalysis.Analyzer): ImageAnalysis {
        analysisUseCase = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            // Use VGA to save bandwidth for the PhotoHandler
            .setTargetResolution(Size(640, 480))
            .build()
        
        analysisUseCase?.setAnalyzer(analysisExecutor, analyzer)
        return analysisUseCase!!
    }

    /**
     * Creates and returns the Preview UseCase.
     */
    fun createPreview(surfaceProvider: Preview.SurfaceProvider): Preview {
        previewUseCase = Preview.Builder().build()
        previewUseCase?.setSurfaceProvider(surfaceProvider)
        return previewUseCase!!
    }
    
    fun shutdown() {
        analysisExecutor.shutdown()
    }
}
