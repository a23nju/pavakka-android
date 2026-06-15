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

    private val _recentFoods = MutableStateFlow<List<FoodItem>>(emptyList())
    val recentFoods: StateFlow<List<FoodItem>> = _recentFoods

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate

    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val dateString: String get() = fmt.format(_selectedDate.value)

    val dateLabel: String
        get() {
            val todayStr = fmt.format(Date())
            return if (dateString == todayStr) "Today" else SimpleDateFormat("EEE, MMM d", Locale.US).format(_selectedDate.value)
        }

    init { load(); loadRecent() }

    fun changeDay(delta: Int) {
        val cal = java.util.Calendar.getInstance()
        cal.time = _selectedDate.value
        cal.add(java.util.Calendar.DAY_OF_MONTH, delta)
        _selectedDate.value = cal.time
        load()
    }

    fun load() {
        viewModelScope.launch {
            try { _entries.value = NetworkService.api.getDiary(dateString) } catch (_: Exception) {}
        }
    }

    fun loadRecent() {
        viewModelScope.launch {
            try { _recentFoods.value = NetworkService.api.getRecentFoods() } catch (_: Exception) {}
        }
    }

    fun copyYesterday() {
        viewModelScope.launch {
            try { NetworkService.api.copyDay(mapOf("date" to dateString)); load() } catch (_: Exception) {}
        }
    }

    fun editEntry(id: String, quantity: Double) {
        viewModelScope.launch {
            try { NetworkService.api.editEntry(ai.laennec.pavakka.core.models.EditEntryRequest(id, quantity)); load() }
            catch (_: Exception) {}
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
                    LogFoodRequest(foodId = food.id, quantity = quantity, meal = meal, date = dateString)
                } else {
                    val payload = LoggedFoodPayload(
                        name = food.name, brand = food.brand, barcode = food.barcode,
                        servingSize = food.servingSize, servingUnit = food.servingUnit,
                        calories = food.calories, protein = food.protein, carbs = food.carbs, fat = food.fat,
                        source = food.source)
                    LogFoodRequest(food = payload, grams = food.servingSize * quantity, meal = meal, date = dateString)
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
