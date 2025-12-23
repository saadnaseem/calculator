package com.example.calculator.vm

import com.example.calculator.engine.AngleMode

data class HistoryEntry(
    val expression: String,
    val result: String? = null,
    val timestamp: String? = null
)

data class CalculatorUiState(
    val expression: String = "",
    val angleMode: AngleMode = AngleMode.DEG,
    val history: List<HistoryEntry> = emptyList(),
    val isHistoryOpen: Boolean = false,
    val result: String? = null,
    val errorMessage: String? = null,
    val ansValue: Double = 0.0
)

