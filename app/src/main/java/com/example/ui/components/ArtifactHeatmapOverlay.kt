package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SuspiciousArtifact
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.VerdictFakeRed

@Composable
fun ArtifactHeatmapOverlay(
    artifacts: List<SuspiciousArtifact>,
    showHeatmap: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = showHeatmap && artifacts.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                artifacts.forEach { artifact ->
                    val boxW = canvasW * artifact.normWidth
                    val boxH = canvasH * artifact.normHeight
                    val left = (canvasW * artifact.normX) - (boxW / 2f)
                    val top = (canvasH * artifact.normY) - (boxH / 2f)

                    val boxColor = when (artifact.severity.lowercase()) {
                        "high" -> VerdictFakeRed
                        "medium" -> NeonPink
                        else -> NeonCyan
                    }

                    // Translucent highlight fill
                    drawRect(
                        color = boxColor.copy(alpha = 0.18f),
                        topLeft = Offset(left, top),
                        size = Size(boxW, boxH)
                    )

                    // Dashed neon cyber box border
                    drawRect(
                        color = boxColor,
                        topLeft = Offset(left, top),
                        size = Size(boxW, boxH),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )

                    // Corner crosshairs
                    val chLen = 14f
                    drawLine(boxColor, Offset(left, top), Offset(left + chLen, top), 3f)
                    drawLine(boxColor, Offset(left, top), Offset(left, top + chLen), 3f)
                    drawLine(boxColor, Offset(left + boxW, top), Offset(left + boxW - chLen, top), 3f)
                    drawLine(boxColor, Offset(left + boxW, top), Offset(left + boxW, top + chLen), 3f)
                    drawLine(boxColor, Offset(left, top + boxH), Offset(left + chLen, top + boxH), 3f)
                    drawLine(boxColor, Offset(left, top + boxH), Offset(left, top + boxH - chLen), 3f)
                    drawLine(boxColor, Offset(left + boxW, top + boxH), Offset(left + boxW - chLen, top + boxH), 3f)
                    drawLine(boxColor, Offset(left + boxW, top + boxH), Offset(left + boxW, top + boxH - chLen), 3f)
                }
            }

            // High-tech badge overlay in bottom corner
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(6.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "HUD SCAN: ${artifacts.size} ANOMALIES",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
