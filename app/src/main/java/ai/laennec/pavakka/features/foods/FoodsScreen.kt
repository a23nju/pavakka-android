package ai.laennec.pavakka.features.foods

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.models.CreateFoodRequest
import ai.laennec.pavakka.core.models.FoodItem
import ai.laennec.pavakka.core.services.NetworkService
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FoodsViewModel : ViewModel() {
    private val _foods = MutableStateFlow<List<FoodItem>>(emptyList())
    val foods: StateFlow<List<FoodItem>> = _foods

    init { load() }

    fun load() {
        viewModelScope.launch {
            try { _foods.value = NetworkService.api.getFoods() } catch (_: Exception) {}
        }
    }

    fun create(name: String, brand: String, calories: Int, protein: Double, carbs: Double, fat: Double) {
        viewModelScope.launch {
            try {
                NetworkService.api.createFood(CreateFoodRequest(
                    name, brand.ifBlank { null }, 100.0, "g", calories, protein, carbs, fat))
                load()
            } catch (_: Exception) {}
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try { NetworkService.api.deleteFood(id); _foods.value = _foods.value.filter { it.id != id } }
            catch (_: Exception) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodsScreen(vm: FoodsViewModel = viewModel()) {
    val foods by vm.foods.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Foods") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }, containerColor = BrandGreen) {
                Icon(Icons.Filled.Add, "Add", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (foods.isEmpty()) {
                item { Text("No custom foods yet. Tap + to add one.", color = Color.Gray) }
            }
            items(foods) { food ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text(food.name, fontWeight = FontWeight.Medium)
                    Text("${food.calories} kcal · P${food.protein.toInt()} C${food.carbs.toInt()} F${food.fat.toInt()}",
                        fontSize = 12.sp, color = Color.Gray)
                }
                Divider()
            }
        }
    }

    if (showAdd) AddFoodDialog(onDismiss = { showAdd = false }) { n, b, cal, p, c, f ->
        vm.create(n, b, cal, p, c, f); showAdd = false
    }
}

@Composable
private fun AddFoodDialog(onDismiss: () -> Unit, onSave: (String, String, Int, Double, Double, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    val num = KeyboardOptions(keyboardType = KeyboardType.Number)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(brand, { brand = it }, label = { Text("Brand (optional)") }, singleLine = true)
                OutlinedTextField(calories, { calories = it }, label = { Text("Calories (per 100g)") }, keyboardOptions = num, singleLine = true)
                OutlinedTextField(protein, { protein = it }, label = { Text("Protein g") }, keyboardOptions = num, singleLine = true)
                OutlinedTextField(carbs, { carbs = it }, label = { Text("Carbs g") }, keyboardOptions = num, singleLine = true)
                OutlinedTextField(fat, { fat = it }, label = { Text("Fat g") }, keyboardOptions = num, singleLine = true)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, brand, calories.toIntOrNull() ?: 0, protein.toDoubleOrNull() ?: 0.0, carbs.toDoubleOrNull() ?: 0.0, fat.toDoubleOrNull() ?: 0.0) },
                enabled = name.isNotBlank() && (calories.toIntOrNull() ?: 0) > 0
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
