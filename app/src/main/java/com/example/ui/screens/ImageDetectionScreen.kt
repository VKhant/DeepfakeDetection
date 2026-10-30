package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.AnalysisReport
import com.example.model.DetectionVerdict
import com.example.ui.components.ArtifactHeatmapOverlay
import com.example.ui.components.ConfidenceGauge
import com.example.ui.components.CyberCard
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.VerdictBadge
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VerdictFakeRed
import com.example.ui.viewmodel.AnalysisState
import com.example.ui.viewmodel.DetectionViewModel

@Composable
fun ImageDetectionScreen(
    viewModel: DetectionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedBitmap by viewModel.selectedImageBitmap.collectAsState()
    val fileName by viewModel.imageFileName.collectAsState()
    val fileSize by viewModel.imageFileSize.collectAsState()
    val analysisState by viewModel.imageAnalysisState.collectAsState()
    val showHeatmap by viewModel.showHeatmap.collectAsState()

    // Camera capture contract
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.onCameraImageCaptured()
        }
    }

    // Camera permission request
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = viewModel.createCameraImageUri()
            takePictureLauncher.launch(uri)
        }
    }

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val uri = viewModel.createCameraImageUri()
            takePictureLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Photo picker contract (Gallery)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            var name = "uploaded_image.png"
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
            viewModel.onImageSelected(uri, name, size)
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
            text = "IMAGE DEEPFAKE SCANNER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Live Camera & Photo Forensics",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        // No image selected: Upload / Live Camera Card
        if (selectedBitmap == null) {
            CyberCard(
                modifier = Modifier.fillMaxWidth(),
                glowGradient = Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.5f), NeonPurple.copy(alpha = 0.5f)))
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Capture or Select Image to Analyze",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Use live camera or pick JPG, PNG, WEBP files",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { launchCamera() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color(0xFF04101A)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("camera_capture_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Live Camera", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("gallery_picker_button")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("From Gallery", fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            // Image Preview Card with Heatmap Overlay
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                    ) {
                        Image(
                            bitmap = selectedBitmap!!.asImageBitmap(),
                            contentDescription = "Selected Image Preview",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        if (analysisState is AnalysisState.Success) {
                            val report = (analysisState as AnalysisState.Success).report
                            ArtifactHeatmapOverlay(
                                artifacts = report.artifacts,
                                showHeatmap = showHeatmap,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // File Metadata & Retake Options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fileName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            val kb = fileSize / 1024
                            val sizeStr = if (kb > 1024) "%.1f MB".format(kb / 1024f) else if (kb > 0) "$kb KB" else "Live Snapshot"
                            Text(
                                text = "$sizeStr • ${selectedBitmap!!.width}x${selectedBitmap!!.height}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (analysisState !is AnalysisState.Analyzing) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { launchCamera() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retake", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Change", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analysis Action / State Views
            when (val state = analysisState) {
                is AnalysisState.Idle -> {
                    Button(
                        onClick = { viewModel.analyzeSelectedImage() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color(0xFF04101A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("analyze_image_button")
                    ) {
                        Text(
                            text = "START FORENSIC ANALYSIS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                is AnalysisState.Analyzing -> {
                    CyberCard(
                        modifier = Modifier.fillMaxWidth(),
                        glowGradient = Brush.linearGradient(listOf(NeonCyan, NeonPurple))
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
                                text = "SCANNING FOR DEEPFAKE ARTIFACTS...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
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
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                        }
                    }
                }

                is AnalysisState.Success -> {
                    ImageResultsDashboard(
                        report = state.report,
                        showHeatmap = showHeatmap,
                        onToggleHeatmap = { viewModel.toggleHeatmap() },
                        onAnalyzeAnother = { viewModel.resetImageState() }
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
                                    text = "Analysis Error",
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
                                onClick = { viewModel.analyzeSelectedImage() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        DisclaimerBanner()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ImageResultsDashboard(
    report: AnalysisReport,
    showHeatmap: Boolean,
    onToggleHeatmap: () -> Unit,
    onAnalyzeAnother: () -> Unit
) {
    Column {
        CyberCard(
            modifier = Modifier.fillMaxWidth(),
            glowGradient = Brush.linearGradient(
                when (report.verdict) {
                    DetectionVerdict.LIKELY_REAL -> listOf(Color(0xFF10B981).copy(alpha = 0.5f), NeonCyan.copy(alpha = 0.5f))
                    DetectionVerdict.LIKELY_MANIPULATED -> listOf(VerdictFakeRed.copy(alpha = 0.6f), NeonPurple.copy(alpha = 0.6f))
                    DetectionVerdict.INCONCLUSIVE -> listOf(Color(0xFFF59E0B).copy(alpha = 0.5f), NeonCyan.copy(alpha = 0.5f))
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
                            color = MaterialTheme.colorScheme.primary
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

                // On Pre-Trained Model: Percentage of Fake vs Real on Per Image
                com.example.ui.components.ModelPredictionBreakdownBar(
                    fakeProbability = report.metrics.overallSynthesisProbability,
                    mediaType = report.mediaType
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Structured Key-Value Telemetry Table (Inline sentences and numbers)
                Text(
                    text = "FORENSIC TELEMETRY DATA",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                com.example.ui.components.TelemetryRow(
                    label = "Classification Confidence",
                    value = "%.1f%%".format(report.confidenceScore),
                    valueColor = MaterialTheme.colorScheme.primary,
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
                    label = "Suspicious Artifact Regions",
                    value = "${report.artifacts.size} Identified",
                    valueColor = if (report.artifacts.isNotEmpty()) VerdictFakeRed else Color(0xFF10B981),
                    badgeBgColor = MaterialTheme.colorScheme.surfaceVariant
                )
                val kb = report.fileSizeBytes / 1024
                val sizeText = if (kb > 1024) "%.1f MB".format(kb / 1024f) else if (kb > 0) "$kb KB" else "Direct Sensor Capture"
                com.example.ui.components.TelemetryRow(
                    label = "File Size & Source",
                    value = sizeText,
                    badgeBgColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "FINDINGS & EXPLANATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
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
                    title = "Technical Deep-Dive",
                    sentence = report.technicalDetails,
                    indicatorColor = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (report.artifacts.isNotEmpty()) {
            CyberCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Artifact Heatmap Overlay",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${report.artifacts.size} suspicious regions pinpointed",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Switch(
                        checked = showHeatmap,
                        onCheckedChange = { onToggleHeatmap() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        CyberCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "FORENSIC VECTOR METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                MetricBar(
                    label = "Error Level Disparity (ELA)",
                    score = report.metrics.elaDisparityScore,
                    description = "Measures JPEG 8x8 DCT recompression variance across quadrants."
                )
                Spacer(modifier = Modifier.height(10.dp))

                MetricBar(
                    label = "Sensor Noise Inconsistency",
                    score = report.metrics.noiseInconsistencyScore,
                    description = "Measures discrete Laplacian high-pass grain distribution."
                )
                Spacer(modifier = Modifier.height(10.dp))

                MetricBar(
                    label = "Boundary Inpainting Seams",
                    score = report.metrics.facialBoundaryArtifactScore,
                    description = "Evaluates gradient smoothing along silhouette edges."
                )
                Spacer(modifier = Modifier.height(10.dp))

                MetricBar(
                    label = "Color Chroma Gradient",
                    score = report.metrics.chromaGradientScore,
                    description = "Analyzes chrominance-luminance distortion."
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAnalyzeAnother,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("analyze_another_image_button")
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyze Another Image", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MetricBar(
    label: String,
    score: Float,
    description: String
) {
    val scoreColor = if (score > 60f) VerdictFakeRed else if (score < 40f) Color(0xFF10B981) else Color(0xFFF59E0B)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(scoreColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "%.1f%%".format(score),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = scoreColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(scoreColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = description,
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
