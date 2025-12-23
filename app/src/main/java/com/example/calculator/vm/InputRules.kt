package com.example.calculator.vm

private val startOperators = setOf("+", "÷", "×", "^", "!")

private val functionTokens = listOf(
    "sin(",
    "cos(",
    "tan(",
    "asin(",
    "acos(",
    "atan(",
    "ln(",
    "log(",
    "sqrt(",
    "abs(",
    "exp("
)

private val constantTokens = listOf("ANS", "π", "pi", "e")

internal fun applyInputRules(
    expression: String,
    input: String,
    ansLiteral: String = "ANS"
): String {
    val trimmed = expression
    if (trimmed.isBlank()) {
        return when {
            input in startOperators -> ansLiteral + input
            input in functionTokens -> input
            else -> input
        }
    }

    return trimmed + input
}

internal fun smartBackspace(
    expression: String,
    ansLiteral: String = "ANS"
): String {
    if (expression.isEmpty()) return expression

    // If we auto-filled with ANS inside a function, drop the auto-filled payload first.
    val autoAnsSuffix = "$ansLiteral)"
    if (expression.endsWith(autoAnsSuffix)) {
        val withoutPayload = expression.dropLast(autoAnsSuffix.length)
        val matchedFunction = functionTokens.firstOrNull { withoutPayload.endsWith(it) }
        if (matchedFunction != null) {
            return withoutPayload
        }
    }

    val tokensToStrip = buildList {
        addAll(functionTokens)
        addAll(constantTokens)
        addAll(startOperators)
        add(")")
    }.sortedByDescending { it.length }

    for (token in tokensToStrip) {
        if (expression.endsWith(token)) {
            return expression.dropLast(token.length)
        }
    }

    return expression.dropLast(1)
}


