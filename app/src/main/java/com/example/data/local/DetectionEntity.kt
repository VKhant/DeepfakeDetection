package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AnalysisReport
import com.example.model.DetectionVerdict
import com.example.model.ForensicMetrics
import com.example.model.FrameAnalysisResult
import com.example.model.MediaType
import com.example.model.SuspiciousArtifact
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "detection_history")
data class DetectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileName: String,
    val mediaType: String, // "IMAGE" or "VIDEO"
    val fileSizeBytes: Long,
    val timestamp: Long,
    val verdict: String, // "LIKELY_REAL", "LIKELY_MANIPULATED", "INCONCLUSIVE"
    val confidenceScore: Float,
    val modelName: String,
    val summaryExplanation: String,
    val technicalDetails: String,
    val framesAnalyzed: Int,
    val suspiciousFramesCount: Int,
    val artifactsJson: String,
    val frameTimelineJson: String,
    val metricsJson: String,
    val mediaUri: String?
) {
    fun toReport(): AnalysisReport {
        val parsedArtifacts = mutableListOf<SuspiciousArtifact>()
        try {
            val array = JSONArray(artifactsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                parsedArtifacts.add(
                    SuspiciousArtifact(
                        label = obj.optString("label", ""),
                        description = obj.optString("description", ""),
                        severity = obj.optString("severity", "Medium"),
                        regionDescription = obj.optString("regionDescription", ""),
                        normX = obj.optDouble("normX", 0.5).toFloat(),
                        normY = obj.optDouble("normY", 0.5).toFloat(),
                        normWidth = obj.optDouble("normWidth", 0.25).toFloat(),
                        normHeight = obj.optDouble("normHeight", 0.25).toFloat()
                    )
                )
            }
        } catch (_: Exception) {}

        val parsedTimeline = mutableListOf<FrameAnalysisResult>()
        try {
            val array = JSONArray(frameTimelineJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                parsedTimeline.add(
                    FrameAnalysisResult(
                        frameIndex = obj.optInt("frameIndex", i),
                        timestampMs = obj.optLong("timestampMs", 0L),
                        timestampFormatted = obj.optString("timestampFormatted", "0:00"),
                        verdict = try { DetectionVerdict.valueOf(obj.optString("verdict", "INCONCLUSIVE")) } catch (_: Exception) { DetectionVerdict.INCONCLUSIVE },
                        confidenceScore = obj.optDouble("confidenceScore", 50.0).toFloat(),
                        anomalyScore = obj.optDouble("anomalyScore", 50.0).toFloat(),
                        summary = obj.optString("summary", "")
                    )
                )
            }
        } catch (_: Exception) {}

        val parsedMetrics = try {
            val mObj = JSONObject(metricsJson)
            ForensicMetrics(
                elaDisparityScore = mObj.optDouble("ela", 0.0).toFloat(),
                noiseInconsistencyScore = mObj.optDouble("noise", 0.0).toFloat(),
                facialBoundaryArtifactScore = mObj.optDouble("boundary", 0.0).toFloat(),
                chromaGradientScore = mObj.optDouble("chroma", 0.0).toFloat(),
                temporalDiscontinuityScore = mObj.optDouble("temporal", 0.0).toFloat(),
                overallSynthesisProbability = mObj.optDouble("prob", 50.0).toFloat()
            )
        } catch (_: Exception) {
            ForensicMetrics(0f, 0f, 0f, 0f, 0f, confidenceScore)
        }

        return AnalysisReport(
            id = id,
            fileName = fileName,
            mediaType = try { MediaType.valueOf(mediaType) } catch (_: Exception) { MediaType.IMAGE },
            fileSizeBytes = fileSizeBytes,
            timestamp = timestamp,
            verdict = try { DetectionVerdict.valueOf(verdict) } catch (_: Exception) { DetectionVerdict.INCONCLUSIVE },
            confidenceScore = confidenceScore,
            modelName = modelName,
            summaryExplanation = summaryExplanation,
            technicalDetails = technicalDetails,
            framesAnalyzed = framesAnalyzed,
            suspiciousFramesCount = suspiciousFramesCount,
            artifacts = parsedArtifacts,
            frameTimeline = parsedTimeline,
            metrics = parsedMetrics,
            mediaUri = mediaUri
        )
    }

    companion object {
        fun fromReport(report: AnalysisReport): DetectionEntity {
            val artifactsArray = JSONArray()
            report.artifacts.forEach {
                val obj = JSONObject()
                obj.put("label", it.label)
                obj.put("description", it.description)
                obj.put("severity", it.severity)
                obj.put("regionDescription", it.regionDescription)
                obj.put("normX", it.normX.toDouble())
                obj.put("normY", it.normY.toDouble())
                obj.put("normWidth", it.normWidth.toDouble())
                obj.put("normHeight", it.normHeight.toDouble())
                artifactsArray.put(obj)
            }

            val timelineArray = JSONArray()
            report.frameTimeline.forEach {
                val obj = JSONObject()
                obj.put("frameIndex", it.frameIndex)
                obj.put("timestampMs", it.timestampMs)
                obj.put("timestampFormatted", it.timestampFormatted)
                obj.put("verdict", it.verdict.name)
                obj.put("confidenceScore", it.confidenceScore.toDouble())
                obj.put("anomalyScore", it.anomalyScore.toDouble())
                obj.put("summary", it.summary)
                timelineArray.put(obj)
            }

            val mObj = JSONObject()
            mObj.put("ela", report.metrics.elaDisparityScore.toDouble())
            mObj.put("noise", report.metrics.noiseInconsistencyScore.toDouble())
            mObj.put("boundary", report.metrics.facialBoundaryArtifactScore.toDouble())
            mObj.put("chroma", report.metrics.chromaGradientScore.toDouble())
            mObj.put("temporal", report.metrics.temporalDiscontinuityScore.toDouble())
            mObj.put("prob", report.metrics.overallSynthesisProbability.toDouble())

            return DetectionEntity(
                id = report.id,
                fileName = report.fileName,
                mediaType = report.mediaType.name,
                fileSizeBytes = report.fileSizeBytes,
                timestamp = report.timestamp,
                verdict = report.verdict.name,
                confidenceScore = report.confidenceScore,
                modelName = report.modelName,
                summaryExplanation = report.summaryExplanation,
                technicalDetails = report.technicalDetails,
                framesAnalyzed = report.framesAnalyzed,
                suspiciousFramesCount = report.suspiciousFramesCount,
                artifactsJson = artifactsArray.toString(),
                frameTimelineJson = timelineArray.toString(),
                metricsJson = mObj.toString(),
                mediaUri = report.mediaUri
            )
        }
    }
}
