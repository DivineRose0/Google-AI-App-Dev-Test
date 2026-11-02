package com.example.data

import kotlinx.coroutines.flow.Flow

class CalculationHistoryRepository(private val dao: CalculationHistoryDao) {
    val allHistory: Flow<List<CalculationHistoryEntity>> = dao.getAllHistory()

    suspend fun insert(expression: String, result: String) {
        val entity = CalculationHistoryEntity(
            expression = expression,
            result = result,
            timestamp = System.currentTimeMillis()
        )
        dao.insertHistory(entity)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
