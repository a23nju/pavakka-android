package ai.laennec.pavakka.features.progress.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import ai.laennec.pavakka.features.progress.viewmodel.ProgressViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(progressViewModel: ProgressViewModel = viewModel()) {
    var weightInput by remember { mutableStateOf("") }
    val logs by progressViewModel.weightLogs.collectAsState()
    val latestWeight = logs.lastOrNull()?.weightKg

    Scaffold(topBar = { TopAppBar(title = { Text("Progress") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current weight card
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current Weight", color = Color.Gray, fontSize = 14.sp)
                    Text(
                        text = if (latestWeight != null) "%.1f kg".format(latestWeight.toFloat()) else "—",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandGreen
                    )
                }
            }

            // Log weight
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Log Weight", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = {
                                weightInput.toFloatOrNull()?.let {
                                    progressViewModel.logWeight(it)
                                    weightInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
                        ) { Text("Save") }
                    }
                }
            }

            // Weight history
            if (logs.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("History", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        logs.takeLast(7).reversed().forEach { log ->
                            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(log.date, color = Color.Gray, modifier = Modifier.weight(1f))
                                Text("%.1f kg".format(log.weightKg))
                            }
                        }
                    }
                }
            }
        }
    }
}
