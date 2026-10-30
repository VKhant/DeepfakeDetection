package com.example.data.remote

import com.example.model.AnalysisReport
import com.example.model.DetectionVerdict
import com.example.model.ForensicMetrics
import com.example.model.MediaType
import com.example.model.SuspiciousArtifact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DeepfakeApiService(
    private var baseUrl: String = "http://10.0.2.2:8000" // Android emulator default localhost alias
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    fun updateBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }

    fun getBaseUrl(): String = baseUrl

    suspend fun checkHealth(): HealthStatus = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/health")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val json = JSONObject(body)
                    HealthStatus(
                        isOnline = true,
                        modelName = json.optString("model", "Remote PyTorch Backend"),
                        device = json.optString("device", "CPU/GPU"),
                        message = "Connected to inference server"
                    )
                } else {
                    HealthStatus(isOnline = false, message = "HTTP ${response.code}")
                }
            }
        } catch (e: Exception) {
            HealthStatus(isOnline = false, message = e.message ?: "Server unreachable")
        }
    }

    suspend fun analyzeImageRemote(
        imageBytes: ByteArray,
        fileName: String
    ): Result<AnalysisReport> = withContext(Dispatchers.IO) {
        try {
            val mediaType = "image/*".toMediaTypeOrNull()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    imageBytes.toRequestBody(mediaType)
                )
                .build()

            val request = Request.Builder()
                .url("$baseUrl/api/v1/analyze-image")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Inference server error: HTTP ${response.code}"))
                }
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)

                val verdictStr = json.optString("verdict", "INCONCLUSIVE")
                val verdict = try { DetectionVerdict.valueOf(verdictStr) } catch (_: Exception) { DetectionVerdict.INCONCLUSIVE }
                val conf = json.optDouble("confidence_score", 50.0).toFloat()
                val modelName = json.optString("model_name", "Remote AI Deepfake Classifier")
                val summary = json.optString("summary", "Analysis completed via inference backend.")
                val tech = json.optString("technical_details", "")

                val artifacts = mutableListOf<SuspiciousArtifact>()
                val artArray = json.optJSONArray("artifacts") ?: JSONArray()
                for (i in 0 until artArray.length()) {
                    val a = artArray.getJSONObject(i)
                    artifacts.add(
                        SuspiciousArtifact(
                            label = a.optString("label", "Artifact"),
                            description = a.optString("description", ""),
                            severity = a.optString("severity", "Medium"),
                            regionDescription = a.optString("regionDescription", "Detected region")
                        )
                    )
                }

                val mObj = json.optJSONObject("metrics") ?: JSONObject()
                val metrics = ForensicMetrics(
                    elaDisparityScore = mObj.optDouble("ela_score", 45.0).toFloat(),
                    noiseInconsistencyScore = mObj.optDouble("noise_score", 45.0).toFloat(),
                    facialBoundaryArtifactScore = mObj.optDouble("boundary_score", 40.0).toFloat(),
                    chromaGradientScore = mObj.optDouble("chroma_score", 35.0).toFloat(),
                    temporalDiscontinuityScore = 0f,
                    overallSynthesisProbability = mObj.optDouble("synthesis_prob", conf.toDouble()).toFloat()
                )

                val report = AnalysisReport(
                    fileName = fileName,
                    mediaType = MediaType.IMAGE,
                    fileSizeBytes = imageBytes.size.toLong(),
                    verdict = verdict,
                    confidenceScore = conf,
                    modelName = modelName,
                    summaryExplanation = summary,
                    technicalDetails = tech,
                    framesAnalyzed = 1,
                    suspiciousFramesCount = if (verdict == DetectionVerdict.LIKELY_MANIPULATED) 1 else 0,
                    artifacts = artifacts,
                    metrics = metrics
                )
                Result.success(report)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class HealthStatus(
    val isOnline: Boolean,
    val modelName: String = "",
    val device: String = "",
    val message: String = ""
)
