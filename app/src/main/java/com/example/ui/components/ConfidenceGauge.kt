package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DetectionVerdict
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VerdictFakeRed
import com.example.ui.theme.VerdictInconclusiveAmber
import com.example.ui.theme.VerdictRealGreen

@Composable
fun ConfidenceGauge(
    score: Float, // 0..100
    verdict: DetectionVerdict,
    modifier: Modifier = Modifier,
    sizeDp: Int = 140
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(score) {
        animatedProgress.animateTo(
            targetValue = (score / 100f).coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    val (activeBrush, labelColor) = when (verdict) {
        DetectionVerdict.LIKELY_REAL -> Pair(
            Brush.sweepGradient(listOf(VerdictRealGreen, NeonCyan, VerdictRealGreen)),
            VerdictRealGreen
        )
        DetectionVerdict.LIKELY_MANIPULATED -> Pair(
            Brush.sweepGradient(listOf(VerdictFakeRed, NeonPurple, VerdictFakeRed)),
            VerdictFakeRed
        )
        DetectionVerdict.INCONCLUSIVE -> Pair(
            Brush.sweepGradient(listOf(VerdictInconclusiveAmber, Color(0xFFFBBF24), VerdictInconclusiveAmber)),
            VerdictInconclusiveAmber
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(sizeDp.dp)
    ) {
        Canvas(modifier = Modifier.size(sizeDp.dp)) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f
            val centerOffset = center

            // Background track
            drawCircle(
                color = Color(0xFF1E293B),
                radius = radius,
                center = centerOffset,
                style = Stroke(width = strokeWidth)
            )

            // Animated progress arc
            drawArc(
                brush = activeBrush,
                startAngle = -90f,
                sweepAngle = animatedProgress.value * 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = androidx.compose.ui.geometry.Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "%.1f%%".format(score),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "CONFIDENCE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = labelColor
            )
            Text(
                text = "Calibrated",
                fontSize = 9.sp,
                color = TextMuted
            )
        }
    }
}
