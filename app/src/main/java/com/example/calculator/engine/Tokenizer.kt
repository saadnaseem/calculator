package com.example.calculator.engine

object Tokenizer {

    fun tokenize(expression: String): EngineResult<List<InputToken>> {
        val tokens = mutableListOf<InputToken>()
        var index = 0
        val length = expression.length

        while (index < length) {
            val char = expression[index]
            when {
                char.isWhitespace() -> index++

                char.isDigit() || char == '.' -> {
                    val start = index
                    var hasDecimal = char == '.'
                    index++
                    while (index < length) {
                        val c = expression[index]
                        if (c.isDigit()) {
                            index++
                        } else if (c == '.') {
                            if (hasDecimal) return EngineResult.Fail(CalculatorError.Syntax)
                            hasDecimal = true
                            index++
                        } else {
                            break
                        }
                    }
                    val numberText = expression.substring(start, index)
                    if (numberText == "." || numberText == "-." || numberText == "+.") {
                        return EngineResult.Fail(CalculatorError.Syntax)
                    }
                    val value = numberText.toDoubleOrNull() ?: return EngineResult.Fail(CalculatorError.Syntax)
                    tokens.add(InputToken.Number(value))
                }

                char == 'π' -> {
                    tokens.add(InputToken.Constant(ConstantType.PI))
                    index++
                }

                char == '(' -> {
                    tokens.add(InputToken.LeftParen)
                    index++
                }

                char == ')' -> {
                    tokens.add(InputToken.RightParen)
                    index++
                }

                char == ',' -> {
                    tokens.add(InputToken.Comma)
                    index++
                }

                char == '+' -> {
                    tokens.add(InputToken.OperatorSymbol(RawOperator.PLUS))
                    index++
                }

                char == '-' -> {
                    tokens.add(InputToken.OperatorSymbol(RawOperator.MINUS))
                    index++
                }

                char == '×' || char == '*' -> {
                    tokens.add(InputToken.OperatorSymbol(RawOperator.MULTIPLY))
                    index++
                }

                char == '÷' || char == '/' -> {
                    tokens.add(InputToken.OperatorSymbol(RawOperator.DIVIDE))
                    index++
                }

                char == '^' -> {
                    tokens.add(InputToken.OperatorSymbol(RawOperator.POWER))
                    index++
                }

                char == '!' -> {
                    tokens.add(InputToken.OperatorSymbol(RawOperator.FACTORIAL))
                    index++
                }

                char.isLetter() -> {
                    val start = index
                    while (index < length && expression[index].isLetter()) {
                        index++
                    }
                    val word = expression.substring(start, index)
                    val normalized = word.lowercase()
                    when {
                        normalized == "ans" -> tokens.add(InputToken.Ans)
                        normalized == "pi" -> tokens.add(InputToken.Constant(ConstantType.PI))
                        normalized == "e" -> tokens.add(InputToken.Constant(ConstantType.E))
                        else -> {
                            val function = FunctionType.fromName(normalized)
                            if (function != null) {
                                tokens.add(InputToken.FunctionName(function))
                            } else {
                                return EngineResult.Fail(CalculatorError.Syntax)
                            }
                        }
                    }
                }

                else -> return EngineResult.Fail(CalculatorError.Syntax)
            }
        }

        return EngineResult.Ok(tokens)
    }
}

