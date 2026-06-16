package ai.laennec.pavakka.features.diary.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.models.FoodItem
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import ai.laennec.pavakka.features.diary.viewmodel.DiaryViewModel

private val MEALS = listOf("Breakfast" to "breakfast", "Lunch" to "lunch", "Dinner" to "dinner", "Snacks" to "snack")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(diaryViewModel: DiaryViewModel = viewModel()) {
    val entries by diaryViewModel.entries.collectAsState()
    val selectedDate by diaryViewModel.selectedDate.collectAsState()
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diary · ${diaryViewModel.dateLabel}") },
                navigationIcon = {
                    IconButton(onClick = { diaryViewModel.changeDay(-1) }) {
                        Icon(Icons.Filled.KeyboardArrowLeft, "Previous day")
                    }
                },
                actions = {
                    IconButton(onClick = { diaryViewModel.changeDay(1) }) {
                        Icon(Icons.Filled.KeyboardArrowRight, "Next day")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showSearch = true }, containerColor = BrandGreen) {
                Icon(Icons.Filled.Add, contentDescription = "Add food", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            items(MEALS) { (label, key) ->
                val mealEntries = entries.filter { it.meal.equals(key, ignoreCase = true) }
                val mealCalories = mealEntries.sumOf { it.calories }
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            if (mealCalories > 0) Text("$mealCalories cal", color = Color.Gray, fontSize = 13.sp)
                        }
                        if (mealEntries.isEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text("Tap + to add food", color = Color.Gray, fontSize = 13.sp)
                        } else {
                            mealEntries.forEach { entry ->
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(entry.foodName, modifier = Modifier.weight(1f))
                                    Text("${entry.calories} cal", color = Color.Gray, fontSize = 13.sp)
                                    IconButton(onClick = { diaryViewModel.deleteEntry(entry.id) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Filled.Delete, "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (entries.isEmpty()) {
                item {
                    TextButton(onClick = { diaryViewModel.copyYesterday() }) {
                        Text("📋 Copy yesterday's food", color = BrandGreen)
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (showSearch) {
        FoodSearchDialog(
            viewModel = diaryViewModel,
            onDismiss = { showSearch = false },
            onLog = { food, meal -> diaryViewModel.logFood(food, meal); showSearch = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchDialog(viewModel: DiaryViewModel, initialMeal: String = "breakfast", onDismiss: () -> Unit, onLog: (FoodItem, String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var meal by remember { mutableStateOf(initialMeal) }
    val results by viewModel.searchResults.collectAsState()
    val recent by viewModel.recentFoods.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val shown = if (query.isBlank()) recent else results

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Add food") },
        text = {
            Column(modifier = Modifier.heightIn(max = 460.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MEALS.forEach { (label, key) ->
                        FilterChip(selected = meal == key, onClick = { meal = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandGreen))
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; viewModel.search(it) },
                    label = { Text("Search foods") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                if (isSearching) CircularProgressIndicator(color = BrandGreen, modifier = Modifier.size(24.dp))
                if (query.isBlank() && recent.isNotEmpty()) Text("Recent", fontSize = 12.sp, color = Color.Gray)
                LazyColumn {
                    items(shown) { food ->
                        Column(modifier = Modifier.fillMaxWidth().clickable { onLog(food, meal) }.padding(vertical = 8.dp)) {
                            Text(food.name, fontWeight = FontWeight.Medium)
                            Text("${food.calories} kcal / ${food.servingSize.toInt()}${food.servingUnit}" +
                                (food.brand?.let { " · $it" } ?: ""), fontSize = 12.sp, color = Color.Gray)
                        }
                        Divider()
                    }
                }
            }
        }
    )
}
