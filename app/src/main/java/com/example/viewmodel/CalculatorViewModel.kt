package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationHistoryEntity
import com.example.data.CalculationHistoryRepository
import com.example.data.OmniCalcDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CalculationHistoryRepository

    val historyItems: StateFlow<List<CalculationHistoryEntity>>

    init {
        val dao = OmniCalcDatabase.getDatabase(application).calculationHistoryDao()
        repository = CalculationHistoryRepository(dao)
        historyItems = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun saveCalculation(expression: String, result: String) {
        viewModelScope.launch {
            repository.insert(expression, result)
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
