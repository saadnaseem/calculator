package com.example.calculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calculator.vm.CalculatorEvent
import com.example.calculator.vm.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel()
) {
    val state = viewModel.uiState
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (state.isHistoryOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(CalculatorEvent.CloseHistory) },
            sheetState = bottomSheetState
        ) {
            HistoryPanel(
                history = state.history,
                onSelectExpression = { viewModel.onEvent(CalculatorEvent.HistorySelected(it)) },
                onSelectResult = { viewModel.onEvent(CalculatorEvent.HistoryResultSelected(it)) }
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Calculator",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { viewModel.onEvent(CalculatorEvent.ToggleAngleMode) }) {
                        Text(text = if (state.angleMode.name == "DEG") "DEG" else "RAD")
                    }
                    TextButton(onClick = { viewModel.onEvent(CalculatorEvent.ToggleHistory) }) {
                        Text(text = "History")
                    }
                }
            }

            DisplayArea(
                expression = state.expression,
                result = state.result,
                errorMessage = state.errorMessage
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { viewModel.onEvent(CalculatorEvent.Clear) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = { viewModel.onEvent(CalculatorEvent.Backspace) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Backspace")
                }
            }

            CalculatorKeypad(
                onKeyPress = { viewModel.onEvent(CalculatorEvent.Input(it)) },
                onEquals = { viewModel.onEvent(CalculatorEvent.Equals) }
            )
        }
    }
}

@Composable
private fun DisplayArea(
    expression: String,
    result: String?,
    errorMessage: String?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Expression",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = if (expression.isBlank()) "0" else expression,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when {
                errorMessage != null -> "Error"
                !result.isNullOrBlank() -> result
                else -> ""
            },
            style = MaterialTheme.typography.titleMedium,
            color = if (errorMessage != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

