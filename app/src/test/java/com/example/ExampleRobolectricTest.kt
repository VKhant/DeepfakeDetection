package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.DetectionEntity
import com.example.model.AnalysisReport
import com.example.model.DetectionVerdict
import com.example.model.ForensicMetrics
import com.example.model.MediaType
import com.example.model.SuspiciousArtifact
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Deepfake AI", appName)
    }

    @Test
    fun `test report serialization roundtrip`() {
        val originalReport = AnalysisReport(
            id = 42L,
            fileName = "portrait_sample.png",
            mediaType = MediaType.IMAGE,
            fileSizeBytes = 204800L,
            timestamp = 1700000000000L,
            verdict = DetectionVerdict.LIKELY_MANIPULATED,
            confidenceScore = 88.5f,
            modelName = "Forensic Core Test",
            summaryExplanation = "Detected face-swap blending seams.",
            technicalDetails = "ELA: 72%, Noise variance: 84%",
            framesAnalyzed = 1,
            suspiciousFramesCount = 1,
            artifacts = listOf(
                SuspiciousArtifact(
                    label = "Inpainting Seam",
                    description = "Alpha blending detected along jawline.",
                    severity = "High",
                    regionDescription = "Lower jawline",
                    normX = 0.5f,
                    normY = 0.6f
                )
            ),
            metrics = ForensicMetrics(
                elaDisparityScore = 72.0f,
                noiseInconsistencyScore = 84.0f,
                facialBoundaryArtifactScore = 65.0f,
                chromaGradientScore = 30.0f,
                temporalDiscontinuityScore = 0.0f,
                overallSynthesisProbability = 82.0f
            ),
            mediaUri = "content://media/sample.png"
        )

        val entity = DetectionEntity.fromReport(originalReport)
        assertEquals("portrait_sample.png", entity.fileName)
        assertEquals("LIKELY_MANIPULATED", entity.verdict)

        val deserialized = entity.toReport()
        assertEquals(originalReport.id, deserialized.id)
        assertEquals(originalReport.fileName, deserialized.fileName)
        assertEquals(originalReport.verdict, deserialized.verdict)
        assertEquals(originalReport.confidenceScore, deserialized.confidenceScore, 0.01f)
        assertEquals(1, deserialized.artifacts.size)
        assertEquals("Inpainting Seam", deserialized.artifacts[0].label)
        assertEquals(72.0f, deserialized.metrics.elaDisparityScore, 0.01f)
    }
}
