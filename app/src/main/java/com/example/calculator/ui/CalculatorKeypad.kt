package com.example.calculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CalculatorKeypad(
    onKeyPress: (String) -> Unit,
    onEquals: () -> Unit,
    isSecondEnabled: Boolean,
    onToggleSecond: () -> Unit
) {
    val primaryRows = listOf(
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf("0", ".", "(", ")"),
        listOf("+", "^", "!", "=")
    )

    val primaryFunctions = listOf("sin", "cos", "tan", "ln", "log", "sqrt", "abs", "exp")
    val inverseFunctions = listOf("asin", "acos", "atan", "ln", "log", "sqrt", "abs", "exp")
    val constants = listOf("ANS", "π", "e")
    val scientificKeys = (if (isSecondEnabled) inverseFunctions else primaryFunctions) + constants
    val functionKeys = primaryFunctions.toSet() + inverseFunctions.toSet()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Scientific",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = onToggleSecond,
                colors = if (isSecondEnabled) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(text = "2nd")
            }
        }
        scientificKeys.chunked(4).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    CalculatorKeyButton(
                        label = key,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (functionKeys.contains(key)) {
                                onKeyPress("$key(")
                            } else {
                                onKeyPress(key)
                            }
                        }
                    )
                }
                repeat(4 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        Text(
            text = "Keypad",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )

        primaryRows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    if (key == "=") {
                        CalculatorKeyButton(
                            label = key,
                            modifier = Modifier.weight(1f),
                            onClick = onEquals
                        )
                    } else {
                        CalculatorKeyButton(
                            label = key,
                            modifier = Modifier.weight(1f),
                            onClick = { onKeyPress(key) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorKeyButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = label)
    }
}

