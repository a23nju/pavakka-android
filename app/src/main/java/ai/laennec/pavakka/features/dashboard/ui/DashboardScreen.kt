package ai.laennec.pavakka.features.dashboard.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import ai.laennec.pavakka.features.auth.viewmodel.AuthViewModel
import ai.laennec.pavakka.features.dashboard.viewmodel.DashboardViewModel
import ai.laennec.pavakka.features.diary.ui.FoodSearchDialog
import ai.laennec.pavakka.features.diary.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    authViewModel: AuthViewModel,
    dashboardViewModel: DashboardViewModel = viewModel(),
    diaryViewModel: DiaryViewModel = viewModel()
) {
    val user by authViewModel.currentUser.collectAsState()
    val entries by diaryViewModel.entries.collectAsState()
    var addMeal by remember { mutableStateOf<String?>(null) }
    val caloriesEaten by dashboardViewModel.caloriesEaten.collectAsState()
    val calorieGoal by dashboardViewModel.calorieGoal.collectAsState()
    val caloriesBurned by dashboardViewModel.caloriesBurned.collectAsState()
    val waterGlasses by dashboardViewModel.waterGlasses.collectAsState()
    val streak by dashboardViewModel.streak.collectAsState()
    val weeklyBalance by dashboardViewModel.weeklyBalance.collectAsState()
    val todayTarget by dashboardViewModel.todayTarget.collectAsState()
    val protein by dashboardViewModel.protein.collectAsState()
    val carbs by dashboardViewModel.carbs.collectAsState()
    val fat by dashboardViewModel.fat.collectAsState()
    val proteinGoal by dashboardViewModel.proteinGoal.collectAsState()
    val carbsGoal by dashboardViewModel.carbsGoal.collectAsState()
    val fatGoal by dashboardViewModel.fatGoal.collectAsState()
    val weekly by dashboardViewModel.weekly.collectAsState()
    val isRefreshing by dashboardViewModel.isRefreshing.collectAsState()

    // Refresh whenever the Dashboard tab is shown so food logged elsewhere reflects here.
    LaunchedEffect(Unit) { dashboardViewModel.load(); diaryViewModel.load() }

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
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { dashboardViewModel.refresh(); diaryViewModel.load() },
            modifier = Modifier.padding(padding)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hello, ${user?.name ?: "there"} 👋", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                if (streak > 0) {
                    Surface(color = BrandGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)) {
                        Text("🔥 $streak", fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }

            if (todayTarget != 0) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Weekly Bank", fontSize = 12.sp, color = Color.Gray)
                            Text("${if (weeklyBalance >= 0) "+" else ""}$weeklyBalance kcal",
                                fontWeight = FontWeight.SemiBold,
                                color = if (weeklyBalance >= 0) BrandGreen else Color(0xFFFF6B35))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Today's target", fontSize = 12.sp, color = Color.Gray)
                            Text("$todayTarget kcal", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

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

            // Macros (protein / carbs / fat) — matches web + iOS
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MacroBar("Protein", protein, proteinGoal, Color(0xFF3B82F6))
                    MacroBar("Carbs", carbs, carbsGoal, Color(0xFFF59E0B))
                    MacroBar("Fat", fat, fatGoal, Color(0xFFEAB308))
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

            // Today's meals — add to Breakfast / Lunch / Dinner / Snacks right from home
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today's meals", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    MEALS.forEach { (label, key, icon) ->
                        val mealEntries = entries.filter { it.meal.equals(key, ignoreCase = true) }
                        val cals = mealEntries.sumOf { it.calories }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 10.dp)) {
                            Text("$icon  $label", modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
                            if (cals > 0) Text("$cals kcal", color = Color.Gray, fontSize = 13.sp)
                            IconButton(onClick = { addMeal = key }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Add, "Add to $label", tint = BrandGreen)
                            }
                        }
                        mealEntries.forEach { entry ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 8.dp, top = 2.dp)) {
                                Text(entry.foodName, modifier = Modifier.weight(1f), fontSize = 13.sp, color = Color.Gray)
                                Text("${entry.calories} cal", fontSize = 12.sp, color = Color.Gray)
                                IconButton(onClick = { diaryViewModel.deleteEntry(entry.id); dashboardViewModel.load() }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Filled.Delete, "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Weekly calories — last 7 days vs goal
            if (weekly.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("This week", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        WeeklyChart(weekly.map { it.calories }, calorieGoal)
                    }
                }
            }
        }
        }
    }

    addMeal?.let { meal ->
        FoodSearchDialog(
            viewModel = diaryViewModel,
            initialMeal = meal,
            onDismiss = { addMeal = null },
            onLog = { food, m -> diaryViewModel.logFood(food, m); dashboardViewModel.load(); addMeal = null }
        )
    }
}

private val MEALS = listOf(
    Triple("Breakfast", "breakfast", "🌅"),
    Triple("Lunch", "lunch", "☀️"),
    Triple("Dinner", "dinner", "🌙"),
    Triple("Snacks", "snack", "🍎")
)

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

@Composable
fun MacroBar(label: String, value: Double, goal: Int, color: Color) {
    val progress = (value.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier.width(12.dp).height(60.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)).background(color.copy(alpha = 0.2f)))
            Box(modifier = Modifier.width(12.dp).fillMaxHeight(progress).clip(RoundedCornerShape(6.dp)).background(color))
        }
        Spacer(Modifier.height(6.dp))
        Text("${value.toInt()} / ${goal}g", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun WeeklyChart(days: List<Int>, goal: Int) {
    val labels = listOf("M", "T", "W", "T", "F", "S", "S")
    val maxVal = (days.maxOrNull() ?: 0).coerceAtLeast(goal).coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().height(110.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEachIndexed { i, cals ->
            val frac = (cals.toFloat() / maxVal).coerceIn(0f, 1f)
            val over = cals > goal
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Text(if (cals > 0) "$cals" else "", fontSize = 9.sp, color = Color.Gray)
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier.width(16.dp).fillMaxHeight(maxOf(frac, 0.02f))
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (over) Color(0xFFEF4444) else BrandGreen)
                )
                Spacer(Modifier.height(4.dp))
                Text(labels.getOrElse(i) { "" }, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}
