package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AnalysisReport
import com.example.model.MediaType
import com.example.ui.components.CyberCard
import com.example.ui.components.DisclaimerBanner
import com.example.ui.components.VerdictBadge
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VerdictFakeRed
import com.example.ui.viewmodel.DetectionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: DetectionViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.history.collectAsState()
    var selectedMediaTypeFilter by remember { mutableStateOf<MediaType?>(null) }
    var selectedReportForDetail by remember { mutableStateOf<AnalysisReport?>(null) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    val filteredList = history.filter { report ->
        selectedMediaTypeFilter == null || report.mediaType == selectedMediaTypeFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DETECTION AUDIT LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Scan History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            if (history.isNotEmpty()) {
                IconButton(
                    onClick = { showClearConfirmation = true },
                    modifier = Modifier.testTag("clear_history_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear History",
                        tint = VerdictFakeRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filters row
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedMediaTypeFilter == null,
                onClick = { selectedMediaTypeFilter = null },
                label = { Text("All (${history.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            FilterChip(
                selected = selectedMediaTypeFilter == MediaType.IMAGE,
                onClick = { selectedMediaTypeFilter = MediaType.IMAGE },
                label = { Text("Images", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
            FilterChip(
                selected = selectedMediaTypeFilter == MediaType.VIDEO,
                onClick = { selectedMediaTypeFilter = MediaType.VIDEO },
                label = { Text("Videos", fontSize = 11.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(14.dp))
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NeonPurple.copy(alpha = 0.25f),
                    selectedLabelColor = NeonPurple
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Scans Recorded Yet",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Analyzed images and videos will appear here.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { report ->
                    HistoryItemCard(
                        report = report,
                        onOpen = { selectedReportForDetail = report },
                        onDelete = { viewModel.deleteReport(report.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    DisclaimerBanner(
                        customText = "Privacy Notice: All history records are stored purely on-device in local SQLite storage. No media files or biometric records are retained on external cloud servers."
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    // Detail Dialog
    if (selectedReportForDetail != null) {
        val report = selectedReportForDetail!!
        AlertDialog(
            onDismissRequest = { selectedReportForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Forensic Report Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = { selectedReportForDetail = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VerdictBadge(verdict = report.verdict, large = true)
                        Text(
                            text = "%.1f%% Conf.".format(report.confidenceScore),
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // On Pre-Trained Model: Percentage of Fake vs Real on Per Media
                    com.example.ui.components.ModelPredictionBreakdownBar(
                        fakeProbability = report.metrics.overallSynthesisProbability,
                        mediaType = report.mediaType
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "FORENSIC TELEMETRY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val dateFormatted = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(report.timestamp))
                    com.example.ui.components.TelemetryRow(label = "File Name", value = report.fileName)
                    com.example.ui.components.TelemetryRow(label = "Timestamp", value = dateFormatted)
                    com.example.ui.components.TelemetryRow(label = "Media Type", value = report.mediaType.name)
                    com.example.ui.components.TelemetryRow(
                        label = "Confidence Score",
                        value = "%.1f%%".format(report.confidenceScore),
                        valueColor = MaterialTheme.colorScheme.primary,
                        badgeBgColor = MaterialTheme.colorScheme.surfaceVariant,
                        isHighlighted = true
                    )
                    com.example.ui.components.TelemetryRow(
                        label = "Authentic (Real)",
                        value = "%.1f%%".format(100f - report.metrics.overallSynthesisProbability),
                        valueColor = Color(0xFF10B981)
                    )
                    com.example.ui.components.TelemetryRow(
                        label = "Deepfake (Fake)",
                        value = "%.1f%%".format(report.metrics.overallSynthesisProbability),
                        valueColor = VerdictFakeRed
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "FINDINGS & EXPLANATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    com.example.ui.components.BulletSentenceRow(
                        title = "Primary Assessment",
                        sentence = report.summaryExplanation,
                        indicatorColor = MaterialTheme.colorScheme.primary
                    )

                    com.example.ui.components.BulletSentenceRow(
                        title = "Technical Detail",
                        sentence = report.technicalDetails,
                        indicatorColor = NeonCyan
                    )

                    if (report.artifacts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "DETECTED ARTIFACT REGIONS (${report.artifacts.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = VerdictFakeRed
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        report.artifacts.forEach { art ->
                            com.example.ui.components.BulletSentenceRow(
                                title = "${art.label} [${art.severity}]",
                                sentence = art.description,
                                indicatorColor = VerdictFakeRed
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedReportForDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("Clear All History?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("This will permanently delete all saved deepfake forensic audit reports from your device.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VerdictFakeRed, contentColor = Color.White)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun HistoryItemCard(
    report: AnalysisReport,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(report.timestamp))
    val realPct = (100f - report.metrics.overallSynthesisProbability).coerceIn(0f, 100f)
    val fakePct = report.metrics.overallSynthesisProbability.coerceIn(0f, 100f)

    CyberCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (report.mediaType == MediaType.IMAGE) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else NeonPurple.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (report.mediaType == MediaType.IMAGE) Icons.Default.Image else Icons.Default.Movie,
                    contentDescription = null,
                    tint = if (report.mediaType == MediaType.IMAGE) MaterialTheme.colorScheme.primary else NeonPurple,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Line 1: File name and media badge in-line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = report.fileName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (report.mediaType == MediaType.IMAGE) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else NeonPurple.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = report.mediaType.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (report.mediaType == MediaType.IMAGE) MaterialTheme.colorScheme.primary else NeonPurple
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Line 2: Timestamp on left, Confidence rating on right in-line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Confidence: %.1f%%".format(report.confidenceScore),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Line 3: Verdict badge on left, Real/Fake breakdown on right in-line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VerdictBadge(verdict = report.verdict, large = false)
                    Text(
                        text = "Real: %.0f%% • Fake: %.0f%%".format(realPct, fakePct),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (fakePct > 50f) VerdictFakeRed else Color(0xFF10B981)
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
