package ai.laennec.pavakka.features.fasting.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import ai.laennec.pavakka.features.fasting.viewmodel.FastingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastingScreen(fastingViewModel: FastingViewModel = viewModel()) {
    val isRunning by fastingViewModel.isRunning.collectAsState()
    val elapsedSeconds by fastingViewModel.elapsedSeconds.collectAsState()
    val goalHours by fastingViewModel.targetHours.collectAsState()
    // Preset protocols (label -> fasting hours); a "Custom" stepper covers everything else.
    val presets = listOf("16:8" to 16, "18:6" to 18, "20:4" to 20, "OMAD" to 23)

    val goalSeconds = goalHours * 3600
    val progress = (elapsedSeconds.toFloat() / goalSeconds).coerceIn(0f, 1f)

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60

    Scaffold(topBar = { TopAppBar(title = { Text("Fasting Timer") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Preset picker
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { (label, h) ->
                        FilterChip(
                            selected = goalHours == h,
                            onClick = { fastingViewModel.setTargetHours(h) },
                            label = { Text(label) },
                            enabled = !isRunning,
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandGreen)
                        )
                    }
                }
            }

            // Custom target — adjust to any number of hours
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Custom goal", fontWeight = FontWeight.SemiBold)
                        Text("Set your own fasting window", fontSize = 12.sp,
                            color = androidx.compose.ui.graphics.Color.Gray)
                    }
                    IconButton(onClick = { fastingViewModel.setTargetHours(goalHours - 1) }, enabled = !isRunning) {
                        Text("−", fontSize = 24.sp, color = BrandGreen)
                    }
                    Text("${goalHours}h", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                        modifier = Modifier.widthIn(min = 44.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    IconButton(onClick = { fastingViewModel.setTargetHours(goalHours + 1) }, enabled = !isRunning) {
                        Text("+", fontSize = 22.sp, color = BrandGreen)
                    }
                }
            }

            // Timer ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp)) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    drawArc(color = BrandGreen.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 28f, cap = StrokeCap.Round))
                    drawArc(color = BrandGreen, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = Stroke(width = 28f, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%02d:%02d:%02d".format(hours, minutes, seconds), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Text("/ ${goalHours}h goal", fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray)
                }
            }

            // Start/Stop button
            Button(
                onClick = { if (isRunning) fastingViewModel.stop() else fastingViewModel.start() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) MaterialTheme.colorScheme.error else BrandGreen)
            ) {
                Text(if (isRunning) "Stop Fast" else "Start Fast", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
