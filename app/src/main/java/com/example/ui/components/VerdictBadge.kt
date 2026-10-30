package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DetectionVerdict
import com.example.ui.theme.VerdictFakeBg
import com.example.ui.theme.VerdictFakeRed
import com.example.ui.theme.VerdictInconclusiveAmber
import com.example.ui.theme.VerdictInconclusiveBg
import com.example.ui.theme.VerdictRealBg
import com.example.ui.theme.VerdictRealGreen

@Composable
fun VerdictBadge(
    verdict: DetectionVerdict,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val (label, icon, fgColor, bgColor, borderColor) = when (verdict) {
        DetectionVerdict.LIKELY_REAL -> VerdictInfo(
            "Likely Authentic",
            Icons.Default.CheckCircle,
            VerdictRealGreen,
            VerdictRealBg,
            VerdictRealGreen.copy(alpha = 0.5f)
        )
        DetectionVerdict.LIKELY_MANIPULATED -> VerdictInfo(
            "Likely AI / Manipulated",
            Icons.Default.Warning,
            VerdictFakeRed,
            VerdictFakeBg,
            VerdictFakeRed.copy(alpha = 0.5f)
        )
        DetectionVerdict.INCONCLUSIVE -> VerdictInfo(
            "Inconclusive",
            Icons.Default.Help,
            VerdictInconclusiveAmber,
            VerdictInconclusiveBg,
            VerdictInconclusiveAmber.copy(alpha = 0.5f)
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (large) 12.dp else 8.dp))
            .background(bgColor.copy(alpha = 0.65f))
            .border(1.dp, borderColor, RoundedCornerShape(if (large) 12.dp else 8.dp))
            .padding(
                horizontal = if (large) 16.dp else 10.dp,
                vertical = if (large) 8.dp else 4.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = fgColor,
                modifier = Modifier.size(if (large) 20.dp else 14.dp)
            )
            Spacer(modifier = Modifier.width(if (large) 8.dp else 6.dp))
            Text(
                text = label,
                color = fgColor,
                fontSize = if (large) 15.sp else 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class VerdictInfo(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val fgColor: Color,
    val bgColor: Color,
    val borderColor: Color
)
