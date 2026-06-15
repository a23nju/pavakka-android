package ai.laennec.pavakka.features.dashboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import ai.laennec.pavakka.features.auth.viewmodel.AuthViewModel
import ai.laennec.pavakka.features.dashboard.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(authViewModel: AuthViewModel, dashboardViewModel: DashboardViewModel = viewModel()) {
    val user by authViewModel.currentUser.collectAsState()
    val caloriesEaten by dashboardViewModel.caloriesEaten.collectAsState()
    val calorieGoal by dashboardViewModel.calorieGoal.collectAsState()
    val caloriesBurned by dashboardViewModel.caloriesBurned.collectAsState()
    val waterGlasses by dashboardViewModel.waterGlasses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Filled.ExitToApp, "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Hello, ${user?.name ?: "there"} 👋", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            // Calorie Ring
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CalorieRing(eaten = caloriesEaten, goal = calorieGoal, burned = caloriesBurned)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        CalorieStat("Eaten", caloriesEaten)
                        CalorieStat("Goal", calorieGoal)
                        CalorieStat("Burned", caloriesBurned)
                    }
                }
            }

            // Water tracker
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("💧 Water", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(8) { i ->
                            Text(if (i < waterGlasses) "💧" else "○", fontSize = 20.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$waterGlasses / 8 glasses", color = Color.Gray, modifier = Modifier.weight(1f))
                        IconButton(onClick = { dashboardViewModel.removeWater() }) { Text("−", fontSize = 20.sp) }
                        IconButton(onClick = { dashboardViewModel.addWater() }) { Text("+", fontSize = 20.sp, color = BrandGreen) }
                    }
                }
            }
        }
    }
}

@Composable
fun CalorieRing(eaten: Int, goal: Int, burned: Int) {
    val remaining = goal - eaten + burned
    val progress = (eaten.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        Canvas(modifier = Modifier.size(160.dp)) {
            drawArc(color = BrandGreen.copy(alpha = 0.2f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = 24f, cap = StrokeCap.Round))
            drawArc(color = BrandGreen, startAngle = -90f, sweepAngle = 360f * progress, useCenter = false, style = Stroke(width = 24f, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$remaining", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Text("remaining", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CalorieStat(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", fontWeight = FontWeight.SemiBold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
