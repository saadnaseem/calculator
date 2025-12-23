package com.example.calculator.vm

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.calculator.data.HistoryRepository
import com.example.calculator.data.HistoryState
import com.example.calculator.data.HistoryStore
import com.example.calculator.engine.AngleMode
import com.example.calculator.engine.CalculatorEngine
import com.example.calculator.engine.EvaluationResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val HISTORY_CAP = 50

class CalculatorViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    var uiState by mutableStateOf(CalculatorUiState())
        private set

    init {
        viewModelScope.launch {
            historyRepository.state.collectLatest { state ->
                applyPersistedState(state)
            }
        }
    }

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
            CalculatorEvent.ToggleSecond -> {
                uiState = uiState.copy(isSecondEnabled = !uiState.isSecondEnabled)
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
            CalculatorEvent.RequestClearHistory -> {
                uiState = uiState.copy(showClearHistoryDialog = true)
            }
            CalculatorEvent.DismissClearHistory -> {
                uiState = uiState.copy(showClearHistoryDialog = false)
            }
            CalculatorEvent.ConfirmClearHistory -> {
                uiState = uiState.copy(
                    history = emptyList(),
                    showClearHistoryDialog = false
                )
                persistHistory(emptyList(), uiState.angleMode)
            }
        }
    }

    private fun applyPersistedState(state: HistoryState) {
        uiState = uiState.copy(
            history = state.history,
            angleMode = state.angleMode
        )
    }

    private fun handleInput(text: String) {
        val baseExpression = if (uiState.errorMessage != null) "" else uiState.expression
        val newExpression = applyInputRules(baseExpression, text)
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
            uiState = uiState.copy(expression = smartBackspace(uiState.expression))
        }
    }

    private fun toggleAngleMode() {
        val next = if (uiState.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        uiState = uiState.copy(angleMode = next)
        persistHistory(uiState.history, next)
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
                val newEntry = HistoryEntry(
                    expression = expression,
                    result = evalResult.formatted,
                    timestamp = timestampNow()
                )
                val updatedHistory = listOf(newEntry) + uiState.history
                val clippedHistory = updatedHistory.take(HISTORY_CAP)
                uiState = uiState.copy(
                    expression = evalResult.formatted,
                    result = evalResult.formatted,
                    errorMessage = null,
                    ansValue = evalResult.value,
                    history = clippedHistory
                )
                persistHistory(clippedHistory, uiState.angleMode)
            }
            is EvaluationResult.Error -> {
                uiState = uiState.copy(
                    errorMessage = "Error"
                )
            }
        }
    }

    private fun persistHistory(history: List<HistoryEntry>, angleMode: AngleMode) {
        viewModelScope.launch {
            historyRepository.save(history, angleMode)
        }
    }

    private fun timestampNow(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return formatter.format(Date())
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val store = HistoryStore(context.applicationContext)
                    val repository = HistoryRepository(store)
                    @Suppress("UNCHECKED_CAST")
                    return CalculatorViewModel(repository) as T
                }
            }
    }
}

