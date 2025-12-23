package com.example.calculator.vm

sealed class CalculatorEvent {
    data class Input(val text: String) : CalculatorEvent()
    object Clear : CalculatorEvent()
    object Backspace : CalculatorEvent()
    object Equals : CalculatorEvent()
    object ToggleAngleMode : CalculatorEvent()
    object ToggleHistory : CalculatorEvent()
    object CloseHistory : CalculatorEvent()
    data class HistorySelected(val entry: HistoryEntry) : CalculatorEvent()
    data class HistoryResultSelected(val entry: HistoryEntry) : CalculatorEvent()
}

