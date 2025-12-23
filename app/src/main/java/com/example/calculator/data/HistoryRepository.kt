package com.example.calculator.data

import com.example.calculator.engine.AngleMode
import com.example.calculator.vm.HistoryEntry
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val store: HistoryStore) {
    val state: Flow<HistoryState> = store.data

    suspend fun save(history: List<HistoryEntry>, angleMode: AngleMode) {
        store.save(history, angleMode)
    }

    suspend fun clear(angleMode: AngleMode) {
        store.clear(angleMode)
    }
}

