package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color? = null,
    backgroundColor: Color? = null,
    glowGradient: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val effectiveBorderColor = borderColor ?: MaterialTheme.colorScheme.outline
    val effectiveBgColor = backgroundColor ?: MaterialTheme.colorScheme.surfaceVariant

    val borderModifier = if (glowGradient != null) {
        Modifier.border(1.2.dp, glowGradient, shape)
    } else {
        Modifier.border(1.dp, effectiveBorderColor.copy(alpha = 0.5f), shape)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .then(borderModifier)
            .background(effectiveBgColor.copy(alpha = 0.85f))
            .padding(16.dp)
    ) {
        content()
    }
}
