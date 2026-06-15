package ai.laennec.pavakka.features.diary.viewmodel

import androidx.lifecycle.ViewModel
import ai.laennec.pavakka.core.models.DiaryEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DiaryViewModel : ViewModel() {
    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries
}
