package ai.laennec.pavakka.features.diary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.laennec.pavakka.core.models.*
import ai.laennec.pavakka.core.services.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DiaryViewModel : ViewModel() {
    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries

    private val _searchResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val searchResults: StateFlow<List<FoodItem>> = _searchResults

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val today: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    init { load() }

    fun load() {
        viewModelScope.launch {
            try { _entries.value = NetworkService.api.getDiary(today) } catch (_: Exception) {}
        }
    }

    fun search(query: String) {
        if (query.isBlank()) { _searchResults.value = emptyList(); return }
        viewModelScope.launch {
            _isSearching.value = true
            try { _searchResults.value = NetworkService.api.searchFoods(query) }
            catch (_: Exception) { _searchResults.value = emptyList() }
            _isSearching.value = false
        }
    }

    // quantity = number of servings
    fun logFood(food: FoodItem, meal: String, quantity: Double = 1.0) {
        viewModelScope.launch {
            try {
                val req = if (food.id != null) {
                    LogFoodRequest(foodId = food.id, quantity = quantity, meal = meal, date = today)
                } else {
                    val payload = LoggedFoodPayload(
                        name = food.name, brand = food.brand, barcode = food.barcode,
                        servingSize = food.servingSize, servingUnit = food.servingUnit,
                        calories = food.calories, protein = food.protein, carbs = food.carbs, fat = food.fat,
                        source = food.source)
                    LogFoodRequest(food = payload, grams = food.servingSize * quantity, meal = meal, date = today)
                }
                NetworkService.api.logFood(req)
                load()
            } catch (_: Exception) {}
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            try { NetworkService.api.deleteEntry(id); _entries.value = _entries.value.filter { it.id != id } }
            catch (_: Exception) {}
        }
    }
}
