package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaType
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.VerdictFakeBg
import com.example.ui.theme.VerdictFakeRed
import com.example.ui.theme.VerdictRealBg
import com.example.ui.theme.VerdictRealGreen
import kotlin.math.abs
import kotlin.math.max

@Composable
fun ModelPredictionBreakdownBar(
    fakeProbability: Float, // 0..100
    mediaType: MediaType,
    modifier: Modifier = Modifier
) {
    val clampedFake = fakeProbability.coerceIn(0f, 100f)
    val clampedReal = (100f - clampedFake).coerceIn(0f, 100f)

    val animatedReal = remember { Animatable(0f) }
    val animatedFake = remember { Animatable(0f) }

    LaunchedEffect(clampedReal, clampedFake) {
        animatedReal.animateTo(clampedReal, tween(1000, easing = FastOutSlowInEasing))
        animatedFake.animateTo(clampedFake, tween(1000, easing = FastOutSlowInEasing))
    }

    val typeLabel = if (mediaType == MediaType.VIDEO) "Per-Video Analysis" else "Per-Image Analysis"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF090E1F).copy(alpha = 0.92f))
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        VerdictRealGreen.copy(alpha = 0.6f),
                        NeonCyan.copy(alpha = 0.4f),
                        VerdictFakeRed.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
            .testTag("model_real_fake_percentage_section")
    ) {
        Column {
            // Header Row: Title & Subtitle in clean line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "PRE-TRAINED MODEL INFERENCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = NeonCyan
                        )
                        Text(
                            text = "Real vs. Fake Percentage ($typeLabel)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dual Score Cards arranged in an aligned horizontal line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // REAL Percentage Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(VerdictRealBg.copy(alpha = 0.5f))
                        .border(1.dp, VerdictRealGreen.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Real",
                            tint = VerdictRealGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AUTHENTIC (REAL)",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdictRealGreen,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "%.1f%%".format(animatedReal.value),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // FAKE Percentage Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(VerdictFakeBg.copy(alpha = 0.5f))
                        .border(1.dp, VerdictFakeRed.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Fake",
                            tint = VerdictFakeRed,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "DEEPFAKE (FAKE)",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerdictFakeRed,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "%.1f%%".format(animatedFake.value),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comparative Dual-Color Split Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Color(0xFF1E293B))
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    val realWeight = max(0.01f, clampedReal)
                    val fakeWeight = max(0.01f, clampedFake)

                    Box(
                        modifier = Modifier
                            .weight(realWeight)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(VerdictRealGreen, Color(0xFF34D399))
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .weight(fakeWeight)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFF87171), VerdictFakeRed)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Aligned lower percentage indicator line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "◀ %.1f%% Authentic (Real)".format(clampedReal),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdictRealGreen
                )
                Text(
                    text = "%.1f%% Deepfake (Fake) ▶".format(clampedFake),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = VerdictFakeRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Aligned Inline Key-Value Lines
            TelemetryRow(
                label = "Authentic (Real) Score",
                value = "%.1f%%".format(clampedReal),
                valueColor = VerdictRealGreen,
                badgeBgColor = VerdictRealBg.copy(alpha = 0.6f)
            )
            TelemetryRow(
                label = "Deepfake (Fake) Score",
                value = "%.1f%%".format(clampedFake),
                valueColor = VerdictFakeRed,
                badgeBgColor = VerdictFakeBg.copy(alpha = 0.6f)
            )
            TelemetryRow(
                label = "Prediction Separation Margin",
                value = "%.1f%% delta".format(abs(clampedReal - clampedFake)),
                valueColor = NeonCyan,
                badgeBgColor = Color(0xFF0F2B3B)
            )
        }
    }
}
