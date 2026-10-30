package com.example.model

enum class MediaType {
    IMAGE,
    VIDEO
}

enum class DetectionVerdict {
    LIKELY_REAL,
    LIKELY_MANIPULATED,
    INCONCLUSIVE
}

data class SuspiciousArtifact(
    val label: String,
    val description: String,
    val severity: String, // "High", "Medium", "Low"
    val regionDescription: String,
    val normX: Float = 0.5f,
    val normY: Float = 0.5f,
    val normWidth: Float = 0.25f,
    val normHeight: Float = 0.25f
)

data class ForensicMetrics(
    val elaDisparityScore: Float, // Error Level Analysis score (0..100)
    val noiseInconsistencyScore: Float, // High-frequency sensor noise disparity
    val facialBoundaryArtifactScore: Float, // Edge blending & warping artifact score
    val chromaGradientScore: Float, // Chrominance vs luminance distribution
    val temporalDiscontinuityScore: Float = 0f, // Video frame-to-frame jitter
    val overallSynthesisProbability: Float // Calibrated probability of synthetic origin
)

data class FrameAnalysisResult(
    val frameIndex: Int,
    val timestampMs: Long,
    val timestampFormatted: String,
    val verdict: DetectionVerdict,
    val confidenceScore: Float,
    val anomalyScore: Float,
    val summary: String
)

data class AnalysisReport(
    val id: Long = 0,
    val fileName: String,
    val mediaType: MediaType,
    val fileSizeBytes: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val verdict: DetectionVerdict,
    val confidenceScore: Float, // Calibrated percentage (0..100)
    val modelName: String = "Deepfake Forensic Vision Core v2.4",
    val summaryExplanation: String,
    val technicalDetails: String,
    val framesAnalyzed: Int = 1,
    val suspiciousFramesCount: Int = 0,
    val artifacts: List<SuspiciousArtifact> = emptyList(),
    val frameTimeline: List<FrameAnalysisResult> = emptyList(),
    val metrics: ForensicMetrics,
    val mediaUri: String? = null
)
