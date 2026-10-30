package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.DetectionRepository
import com.example.engine.ForensicAnalysisEngine
import com.example.model.AnalysisReport
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class AppThemeMode {
    DARK, LIGHT, SYSTEM
}

enum class DetectionSensitivity {
    BALANCED, STRICT, SENSITIVE
}

sealed interface AnalysisState {
    data object Idle : AnalysisState
    data class Analyzing(val stepDescription: String, val progress: Float) : AnalysisState
    data class Success(val report: AnalysisReport) : AnalysisState
    data class Error(val errorMessage: String) : AnalysisState
}

class DetectionViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = DetectionRepository(database.detectionDao())
    private val prefs = application.getSharedPreferences("deepfake_ai_prefs", Context.MODE_PRIVATE)

    // Theme Mode
    private val _themeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name)
        } catch (_: Exception) {
            AppThemeMode.DARK
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Model Sensitivity Setting
    private val _sensitivity = MutableStateFlow(
        try {
            DetectionSensitivity.valueOf(prefs.getString("sensitivity", DetectionSensitivity.BALANCED.name) ?: DetectionSensitivity.BALANCED.name)
        } catch (_: Exception) {
            DetectionSensitivity.BALANCED
        }
    )
    val sensitivity: StateFlow<DetectionSensitivity> = _sensitivity.asStateFlow()

    // Background Database Auto-Save Toggle
    private val _autoSaveToBackground = MutableStateFlow(
        prefs.getBoolean("auto_save_bg", true)
    )
    val autoSaveToBackground: StateFlow<Boolean> = _autoSaveToBackground.asStateFlow()

    // Auto-analyze on camera capture
    private val _autoAnalyzeOnCapture = MutableStateFlow(
        prefs.getBoolean("auto_analyze_capture", true)
    )
    val autoAnalyzeOnCapture: StateFlow<Boolean> = _autoAnalyzeOnCapture.asStateFlow()

    // History Flow from Room DB
    val history: StateFlow<List<AnalysisReport>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Image Detection State
    private val _imageAnalysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val imageAnalysisState: StateFlow<AnalysisState> = _imageAnalysisState.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _selectedImageBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedImageBitmap: StateFlow<Bitmap?> = _selectedImageBitmap.asStateFlow()

    private val _imageFileName = MutableStateFlow<String>("")
    val imageFileName: StateFlow<String> = _imageFileName.asStateFlow()

    private val _imageFileSize = MutableStateFlow<Long>(0L)
    val imageFileSize: StateFlow<Long> = _imageFileSize.asStateFlow()

    private val _showHeatmap = MutableStateFlow(true)
    val showHeatmap: StateFlow<Boolean> = _showHeatmap.asStateFlow()

    // Video Detection State
    private val _videoAnalysisState = MutableStateFlow<AnalysisState>(AnalysisState.Idle)
    val videoAnalysisState: StateFlow<AnalysisState> = _videoAnalysisState.asStateFlow()

    private val _selectedVideoUri = MutableStateFlow<Uri?>(null)
    val selectedVideoUri: StateFlow<Uri?> = _selectedVideoUri.asStateFlow()

    private val _videoFileName = MutableStateFlow<String>("")
    val videoFileName: StateFlow<String> = _videoFileName.asStateFlow()

    private val _videoFileSize = MutableStateFlow<Long>(0L)
    val videoFileSize: StateFlow<Long> = _videoFileSize.asStateFlow()

    private val _frameIntervalSec = MutableStateFlow(1.0f)
    val frameIntervalSec: StateFlow<Float> = _frameIntervalSec.asStateFlow()

    private val _selectedFrameIndex = MutableStateFlow(0)
    val selectedFrameIndex: StateFlow<Int> = _selectedFrameIndex.asStateFlow()

    // Temporary camera capture URIs
    var tempCameraImageUri: Uri? = null
    var tempCameraVideoUri: Uri? = null

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun setSensitivity(sensitivity: DetectionSensitivity) {
        _sensitivity.value = sensitivity
        prefs.edit().putString("sensitivity", sensitivity.name).apply()
    }

    fun setAutoSaveToBackground(enabled: Boolean) {
        _autoSaveToBackground.value = enabled
        prefs.edit().putBoolean("auto_save_bg", enabled).apply()
    }

    fun setAutoAnalyzeOnCapture(enabled: Boolean) {
        _autoAnalyzeOnCapture.value = enabled
        prefs.edit().putBoolean("auto_analyze_capture", enabled).apply()
    }

    fun toggleHeatmap() {
        _showHeatmap.value = !_showHeatmap.value
    }

    fun setFrameInterval(interval: Float) {
        _frameIntervalSec.value = interval
    }

    fun selectFrame(index: Int) {
        _selectedFrameIndex.value = index
    }

    // Camera Capture Uri Generators
    fun createCameraImageUri(): Uri {
        val context = getApplication<Application>()
        val dir = File(context.cacheDir, "camera_captures").apply { mkdirs() }
        val file = File(dir, "live_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        tempCameraImageUri = uri
        return uri
    }

    fun createCameraVideoUri(): Uri {
        val context = getApplication<Application>()
        val dir = File(context.cacheDir, "camera_captures").apply { mkdirs() }
        val file = File(dir, "live_video_${System.currentTimeMillis()}.mp4")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        tempCameraVideoUri = uri
        return uri
    }

    fun onCameraImageCaptured() {
        val uri = tempCameraImageUri ?: return
        onImageSelected(uri, "Live Camera Photo.jpg", 0L)
        if (_autoAnalyzeOnCapture.value) {
            analyzeSelectedImage()
        }
    }

    fun onCameraVideoCaptured() {
        val uri = tempCameraVideoUri ?: return
        onVideoSelected(uri, "Live Camera Video.mp4", 0L)
        if (_autoAnalyzeOnCapture.value) {
            analyzeSelectedVideo()
        }
    }

    // Image Selection
    fun onImageSelected(uri: Uri, name: String, size: Long) {
        _selectedImageUri.value = uri
        _imageFileName.value = name
        _imageFileSize.value = size
        _imageAnalysisState.value = AnalysisState.Idle

        viewModelScope.launch {
            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                    _selectedImageBitmap.value = ForensicAnalysisEngine.decodeSampledBitmap(stream)
                }
            } catch (_: Exception) {}
        }
    }

    fun analyzeSelectedImage() {
        val uri = _selectedImageUri.value ?: return
        val bitmap = _selectedImageBitmap.value ?: return
        val name = _imageFileName.value.ifBlank { "uploaded_image.png" }
        val size = _imageFileSize.value

        viewModelScope.launch {
            _imageAnalysisState.value = AnalysisState.Analyzing("Initializing forensic optical inspection...", 0.2f)

            try {
                _imageAnalysisState.value = AnalysisState.Analyzing("Running Error Level Analysis (ELA)...", 0.45f)
                kotlinx.coroutines.delay(200)
                _imageAnalysisState.value = AnalysisState.Analyzing("Evaluating Laplacian high-frequency noise variance...", 0.70f)
                kotlinx.coroutines.delay(200)
                _imageAnalysisState.value = AnalysisState.Analyzing("Scanning boundary gradient seams & color chroma...", 0.88f)

                val report = ForensicAnalysisEngine.analyzeImageBitmap(
                    bitmap = bitmap,
                    fileName = name,
                    fileSizeBytes = size,
                    mediaUri = uri.toString()
                )

                // Save to Room DB history in background if enabled
                if (_autoSaveToBackground.value) {
                    repository.saveReport(report)
                }
                _imageAnalysisState.value = AnalysisState.Success(report)
            } catch (e: Exception) {
                _imageAnalysisState.value = AnalysisState.Error(e.localizedMessage ?: "Analysis failed.")
            }
        }
    }

    fun resetImageState() {
        _selectedImageUri.value = null
        _selectedImageBitmap.value = null
        _imageFileName.value = ""
        _imageFileSize.value = 0L
        _imageAnalysisState.value = AnalysisState.Idle
    }

    // Video Selection
    fun onVideoSelected(uri: Uri, name: String, size: Long) {
        _selectedVideoUri.value = uri
        _videoFileName.value = name
        _videoFileSize.value = size
        _videoAnalysisState.value = AnalysisState.Idle
        _selectedFrameIndex.value = 0
    }

    fun analyzeSelectedVideo() {
        val uri = _selectedVideoUri.value ?: return
        val name = _videoFileName.value.ifBlank { "uploaded_video.mp4" }
        val size = _videoFileSize.value
        val interval = _frameIntervalSec.value

        viewModelScope.launch {
            _videoAnalysisState.value = AnalysisState.Analyzing("Initializing video decoder...", 0.05f)

            try {
                val report = ForensicAnalysisEngine.analyzeVideo(
                    context = getApplication(),
                    videoUri = uri,
                    fileName = name,
                    fileSizeBytes = size,
                    frameIntervalSec = interval,
                    onProgress = { step, progress ->
                        _videoAnalysisState.value = AnalysisState.Analyzing(step, progress)
                    }
                )

                // Save to Room DB history in background if enabled
                if (_autoSaveToBackground.value) {
                    repository.saveReport(report)
                }
                _videoAnalysisState.value = AnalysisState.Success(report)
            } catch (e: Exception) {
                _videoAnalysisState.value = AnalysisState.Error(e.localizedMessage ?: "Video analysis failed.")
            }
        }
    }

    fun resetVideoState() {
        _selectedVideoUri.value = null
        _videoFileName.value = ""
        _videoFileSize.value = 0L
        _videoAnalysisState.value = AnalysisState.Idle
        _selectedFrameIndex.value = 0
    }

    // History Actions
    fun deleteReport(id: Long) {
        viewModelScope.launch {
            repository.deleteReport(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
