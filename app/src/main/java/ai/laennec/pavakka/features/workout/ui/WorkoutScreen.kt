package ai.laennec.pavakka.features.workout.ui

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
import ai.laennec.pavakka.features.workout.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(workoutViewModel: WorkoutViewModel = viewModel()) {
    val isRunning by workoutViewModel.isRunning.collectAsState()
    val elapsedSeconds by workoutViewModel.elapsedSeconds.collectAsState()
    val selectedExercise by workoutViewModel.selectedExercise.collectAsState()
    val caloriesBurned by workoutViewModel.caloriesBurned.collectAsState()
    val exercises = listOf("Running", "Walking", "Cycling", "Swimming", "Yoga", "Weight Training", "HIIT")

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60

    Scaffold(topBar = { TopAppBar(title = { Text("Workout") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stopwatch display
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("%02d:%02d:%02d".format(hours, minutes, seconds), fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("🔥 ${caloriesBurned} cal burned", fontSize = 18.sp, color = Color(0xFFFF6B35))
                }
            }

            // Exercise picker
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Exercise", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    exercises.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            row.forEach { ex ->
                                FilterChip(
                                    selected = selectedExercise == ex,
                                    onClick = { workoutViewModel.selectExercise(ex) },
                                    label = { Text(ex, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandGreen)
                                )
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // Start/Stop button
            Button(
                onClick = { if (isRunning) workoutViewModel.stop() else workoutViewModel.start() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) MaterialTheme.colorScheme.error else BrandGreen)
            ) {
                Text(if (isRunning) "Stop Workout" else "Start Workout", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
