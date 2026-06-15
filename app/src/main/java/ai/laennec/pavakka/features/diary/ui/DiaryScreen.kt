package ai.laennec.pavakka.features.diary.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import ai.laennec.pavakka.features.diary.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(diaryViewModel: DiaryViewModel = viewModel()) {
    val meals = listOf("Breakfast", "Lunch", "Dinner", "Snacks")
    val entries by diaryViewModel.entries.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Food Diary") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* open search */ }, containerColor = BrandGreen) {
                Icon(Icons.Filled.Add, contentDescription = "Add food", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            items(meals) { meal ->
                val mealEntries = entries.filter { it.meal == meal }
                val mealCalories = mealEntries.sumOf { it.calories }
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(meal, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            if (mealCalories > 0) Text("${mealCalories} cal", color = Color.Gray, fontSize = 13.sp)
                        }
                        if (mealEntries.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Tap + to add food", color = Color.Gray, fontSize = 13.sp)
                        } else {
                            mealEntries.forEach { entry ->
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                Row {
                                    Text(entry.foodName, modifier = Modifier.weight(1f))
                                    Text("${entry.calories} cal", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}
