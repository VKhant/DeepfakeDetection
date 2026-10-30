package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoCameraFront
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AnalysisReport
import com.example.model.DetectionVerdict
import com.example.ui.components.ConfidenceGauge
import com.example.ui.components.CyberCard
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.VerdictBadge
import com.example.ui.components.VideoTimelineBar
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VerdictFakeRed
import com.example.ui.viewmodel.AnalysisState
import com.example.ui.viewmodel.DetectionViewModel

@Composable
fun VideoDetectionScreen(
    viewModel: DetectionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedVideoUri by viewModel.selectedVideoUri.collectAsState()
    val videoFileName by viewModel.videoFileName.collectAsState()
    val videoFileSize by viewModel.videoFileSize.collectAsState()
    val analysisState by viewModel.videoAnalysisState.collectAsState()
    val frameInterval by viewModel.frameIntervalSec.collectAsState()
    val selectedFrameIndex by viewModel.selectedFrameIndex.collectAsState()

    // Video recording contract
    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            viewModel.onCameraVideoCaptured()
        }
    }

    // Camera permission contract
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = viewModel.createCameraVideoUri()
            captureVideoLauncher.launch(uri)
        }
    }

    fun launchVideoRecorder() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val uri = viewModel.createCameraVideoUri()
            captureVideoLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Video Picker contract (Gallery)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            var name = "uploaded_video.mp4"
            var size = 0L
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nIdx != -1) name = cursor.getString(nIdx)
                        if (sIdx != -1) size = cursor.getLong(sIdx)
                    }
                }
            } catch (_: Exception) {}
            viewModel.onVideoSelected(uri, name, size)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "VIDEO DEEPFAKE SCANNER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = NeonPurple
        )
        Text(
            text = "Live Video & Clip Forensics",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // No video selected: Upload / Record Card
        if (selectedVideoUri == null) {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                glowGradient = Brush.linearGradient(listOf(NeonPurple.copy(alpha = 0.5f), NeonPink.copy(alpha = 0.5f)))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(NeonPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoCameraFront,
                            contentDescription = "Live Video",
                            tint = NeonPurple,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Record or Select Video to Analyze",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Supports live recording or MP4, MOV, WEBM files",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { launchVideoRecorder() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("record_video_camera_button")
                        ) {
                            Icon(Icons.Default.VideoCameraFront, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record Live", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("browse_video_button")
                        ) {
                            Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Browse Video", fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            // Video Selected Preview Card
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF070B19)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleOutline,
                            contentDescription = "Video Ready",
                            tint = NeonPurple,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = videoFileName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            val kb = videoFileSize / 1024
                            val sizeStr = if (kb > 1024) "%.1f MB".format(kb / 1024f) else if (kb > 0) "$kb KB" else "Live Recorded Clip"
                            Text(
                                text = "$sizeStr • Video Stream",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (analysisState !is AnalysisState.Analyzing) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { launchVideoRecorder() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.VideoCameraFront, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Re-record", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        videoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Change", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Frame Extraction Interval Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Frame Sampling Rate:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.5f to "0.5s (Dense)", 1.0f to "1.0s (Normal)", 2.0f to "2.0s (Fast)").forEach { (interval, label) ->
                            FilterChip(
                                selected = frameInterval == interval,
                                onClick = { if (analysisState !is AnalysisState.Analyzing) viewModel.setFrameInterval(interval) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple.copy(alpha = 0.25f),
                                    selectedLabelColor = NeonPurple
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Action / State Views
            when (val state = analysisState) {
                is AnalysisState.Idle -> {
                    Button(
                        onClick = { viewModel.analyzeSelectedVideo() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("analyze_video_button")
                    ) {
                        Text(
                            text = "START TEMPORAL VIDEO AUDIT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                is AnalysisState.Analyzing -> {
                    CyberCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowGradient = Brush.linearGradient(listOf(NeonPurple, NeonPink))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val pulseAlpha by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )

                            Text(
                                text = "EXTRACTING & AUDITING VIDEO FRAMES...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = NeonPurple.copy(alpha = pulseAlpha)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.stepDescription,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { state.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = NeonPurple,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }

                is AnalysisState.Success -> {
                    VideoResultsDashboard(
                        report = state.report,
                        selectedFrameIndex = selectedFrameIndex,
                        onSelectFrame = { viewModel.selectFrame(it) },
                        onAnalyzeAnother = { viewModel.resetVideoState() }
                    )
                }

                is AnalysisState.Error -> {
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = VerdictFakeRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Video Audit Failed",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VerdictFakeRed
                                )
                                Text(
                                    text = state.errorMessage,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { viewModel.analyzeSelectedVideo() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple, contentColor = Color.White)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        DisclaimerBanner(
            customText = "Note: Video deepfake analysis inspects visual frames for warping, blending seams, and temporal flicker. Audio deepfake analysis is not performed by this visual engine."
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun VideoResultsDashboard(
    report: AnalysisReport,
    selectedFrameIndex: Int,
    onSelectFrame: (Int) -> Unit,
    onAnalyzeAnother: () -> Unit
) {
    Column {
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            glowGradient = Brush.linearGradient(
                when (report.verdict) {
                    DetectionVerdict.LIKELY_REAL -> listOf(Color(0xFF10B981).copy(alpha = 0.5f), NeonCyan.copy(alpha = 0.5f))
                    DetectionVerdict.LIKELY_MANIPULATED -> listOf(VerdictFakeRed.copy(alpha = 0.6f), NeonPurple.copy(alpha = 0.6f))
                    DetectionVerdict.INCONCLUSIVE -> listOf(Color(0xFFF59E0B).copy(alpha = 0.5f), NeonPurple.copy(alpha = 0.5f))
                }
            )
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PREDICTION VERDICT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = NeonPurple
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        VerdictBadge(verdict = report.verdict, large = true)
                    }

                    ConfidenceGauge(
                        score = report.confidenceScore,
                        verdict = report.verdict,
                        sizeDp = 110
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // On Pre-Trained Model: Percentage of Fake vs Real on Per Video
                com.example.ui.components.ModelPredictionBreakdownBar(
                    fakeProbability = report.metrics.overallSynthesisProbability,
                    mediaType = report.mediaType
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatPill(
                        label = "Frames Analyzed",
                        value = "${report.framesAnalyzed}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        label = "Suspicious Frames",
                        value = "${report.suspiciousFramesCount}",
                        color = if (report.suspiciousFramesCount > 0) VerdictFakeRed else Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                    StatPill(
                        label = "Temporal Jitter",
                        value = "%.0f%%".format(report.metrics.temporalDiscontinuityScore),
                        color = if (report.metrics.temporalDiscontinuityScore > 50f) VerdictFakeRed else NeonPurple,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Structured Video Forensic Telemetry Rows
                Text(
                    text = "VIDEO FORENSIC TELEMETRY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = NeonPurple
                )
                Spacer(modifier = Modifier.height(6.dp))

                com.example.ui.components.TelemetryRow(
                    label = "Calibrated Confidence",
                    value = "%.1f%%".format(report.confidenceScore),
                    valueColor = NeonPurple,
                    badgeBgColor = MaterialTheme.colorScheme.surfaceVariant,
                    isHighlighted = true
                )
                com.example.ui.components.TelemetryRow(
                    label = "Authentic (Real) Likelihood",
                    value = "%.1f%%".format(100f - report.metrics.overallSynthesisProbability),
                    valueColor = Color(0xFF10B981),
                    badgeBgColor = Color(0xFF063321)
                )
                com.example.ui.components.TelemetryRow(
                    label = "Deepfake (Fake) Likelihood",
                    value = "%.1f%%".format(report.metrics.overallSynthesisProbability),
                    valueColor = VerdictFakeRed,
                    badgeBgColor = Color(0xFF3B0B13)
                )
                com.example.ui.components.TelemetryRow(
                    label = "Inter-Frame Optical Discontinuity",
                    value = "%.1f%%".format(report.metrics.temporalDiscontinuityScore),
                    valueColor = if (report.metrics.temporalDiscontinuityScore > 50f) VerdictFakeRed else NeonCyan,
                    badgeBgColor = MaterialTheme.colorScheme.surfaceVariant
                )
                com.example.ui.components.TelemetryRow(
                    label = "Facial Boundary Deformation",
                    value = "%.1f%%".format(report.metrics.facialBoundaryArtifactScore),
                    valueColor = if (report.metrics.facialBoundaryArtifactScore > 50f) VerdictFakeRed else Color(0xFF10B981),
                    badgeBgColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "FINDINGS & EXPLANATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = NeonPurple
                )
                Spacer(modifier = Modifier.height(6.dp))

                com.example.ui.components.BulletSentenceRow(
                    title = "Primary Assessment",
                    sentence = report.summaryExplanation,
                    indicatorColor = when (report.verdict) {
                        DetectionVerdict.LIKELY_REAL -> Color(0xFF10B981)
                        DetectionVerdict.LIKELY_MANIPULATED -> VerdictFakeRed
                        DetectionVerdict.INCONCLUSIVE -> Color(0xFFF59E0B)
                    }
                )

                com.example.ui.components.BulletSentenceRow(
                    title = "Frame Analysis Summary",
                    sentence = report.technicalDetails,
                    indicatorColor = NeonPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (report.frameTimeline.isNotEmpty()) {
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                VideoTimelineBar(
                    timeline = report.frameTimeline,
                    selectedIndex = selectedFrameIndex,
                    onSelectFrame = onSelectFrame
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onAnalyzeAnother,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("analyze_another_video_button")
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyze Another Video", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
