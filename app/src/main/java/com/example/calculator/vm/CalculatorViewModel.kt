package com.example.calculator.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.CalculatorEngine
import com.example.calculator.engine.EvaluationResult

private const val HISTORY_CAP = 50

class CalculatorViewModel : ViewModel() {

    var uiState by mutableStateOf(CalculatorUiState())
        private set

    fun onEvent(event: CalculatorEvent) {
        when (event) {
            is CalculatorEvent.Input -> handleInput(event.text)
            CalculatorEvent.Clear -> clear()
            CalculatorEvent.Backspace -> backspace()
            CalculatorEvent.Equals -> evaluate()
            CalculatorEvent.ToggleAngleMode -> toggleAngleMode()
            CalculatorEvent.ToggleHistory -> {
                uiState = uiState.copy(isHistoryOpen = !uiState.isHistoryOpen)
            }
            CalculatorEvent.CloseHistory -> {
                uiState = uiState.copy(isHistoryOpen = false)
            }
            is CalculatorEvent.HistorySelected -> {
                uiState = uiState.copy(
                    expression = event.entry.expression,
                    errorMessage = null,
                    isHistoryOpen = false
                )
            }
            is CalculatorEvent.HistoryResultSelected -> {
                val value = event.entry.result
                val parsed = value?.toDoubleOrNull()
                uiState = uiState.copy(
                    expression = value ?: "",
                    ansValue = parsed ?: uiState.ansValue,
                    result = value,
                    errorMessage = null,
                    isHistoryOpen = false
                )
            }
        }
    }

    private fun handleInput(text: String) {
        val newExpression = if (uiState.errorMessage != null) {
            text
        } else {
            uiState.expression + text
        }
        uiState = uiState.copy(
            expression = newExpression,
            errorMessage = null
        )
    }

    private fun clear() {
        uiState = uiState.copy(
            expression = "",
            result = null,
            errorMessage = null
        )
    }

    private fun backspace() {
        if (uiState.errorMessage != null) {
            uiState = uiState.copy(errorMessage = null)
            return
        }
        if (uiState.expression.isNotEmpty()) {
            uiState = uiState.copy(expression = uiState.expression.dropLast(1))
        }
    }

    private fun toggleAngleMode() {
        val next = if (uiState.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        uiState = uiState.copy(angleMode = next)
    }

    private fun evaluate() {
        val expression = uiState.expression.trim()
        if (expression.isEmpty()) return

        val evalResult = CalculatorEngine.evaluate(
            expression = expression,
            angleMode = uiState.angleMode,
            lastAnswer = uiState.ansValue
        )

        when (evalResult) {
            is EvaluationResult.Success -> {
                val updatedHistory = listOf(
                    HistoryEntry(expression = expression, result = evalResult.formatted)
                ) + uiState.history
                uiState = uiState.copy(
                    expression = evalResult.formatted,
                    result = evalResult.formatted,
                    errorMessage = null,
                    ansValue = evalResult.value,
                    history = updatedHistory.take(HISTORY_CAP)
                )
            }
            is EvaluationResult.Error -> {
                uiState = uiState.copy(
                    errorMessage = "Error"
                )
            }
        }
    }
}

