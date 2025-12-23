package com.example.calculator.engine

private data class ParenContext(
    val isFunction: Boolean,
    val function: FunctionType?,
    var argCount: Int = 1
)

private sealed class StackToken {
    data object LeftParen : StackToken()
    data class OperatorToken(val operator: Operator) : StackToken()
    data class FunctionToken(val function: FunctionType) : StackToken()
}

object ParserShuntingYard {

    fun toRpn(tokens: List<InputToken>): EngineResult<List<RpnToken>> {
        val output = mutableListOf<RpnToken>()
        val operatorStack = mutableListOf<StackToken>()
        val parenStack = mutableListOf<ParenContext>()

        var expectingOperand = true
        var pendingFunctionParen = false

        for (token in tokens) {
            if (pendingFunctionParen && token !is InputToken.LeftParen) {
                return EngineResult.Fail(CalculatorError.Syntax)
            }

            when (token) {
                is InputToken.Number -> {
                    if (!expectingOperand) return EngineResult.Fail(CalculatorError.Syntax)
                    output.add(RpnToken.Number(token.value))
                    expectingOperand = false
                    pendingFunctionParen = false
                }

                is InputToken.Constant -> {
                    if (!expectingOperand) return EngineResult.Fail(CalculatorError.Syntax)
                    output.add(RpnToken.Number(token.constant.value))
                    expectingOperand = false
                    pendingFunctionParen = false
                }

                InputToken.Ans -> {
                    if (!expectingOperand) return EngineResult.Fail(CalculatorError.Syntax)
                    output.add(RpnToken.Ans)
                    expectingOperand = false
                    pendingFunctionParen = false
                }

                is InputToken.FunctionName -> {
                    if (!expectingOperand) return EngineResult.Fail(CalculatorError.Syntax)
                    operatorStack.add(StackToken.FunctionToken(token.function))
                    pendingFunctionParen = true
                }

                InputToken.LeftParen -> {
                    val functionToken = operatorStack.lastOrNull() as? StackToken.FunctionToken
                    val isFunctionParen = pendingFunctionParen && functionToken != null
                    parenStack.add(
                        ParenContext(
                            isFunction = isFunctionParen,
                            function = functionToken?.function,
                            argCount = 1
                        )
                    )
                    operatorStack.add(StackToken.LeftParen)
                    expectingOperand = true
                    pendingFunctionParen = false
                }

                InputToken.RightParen -> {
                    if (pendingFunctionParen) return EngineResult.Fail(CalculatorError.Syntax)
                    if (expectingOperand) return EngineResult.Fail(CalculatorError.Syntax)
                    var foundParen = false
                    while (operatorStack.isNotEmpty()) {
                        when (val top = operatorStack.popLast()) {
                            is StackToken.LeftParen -> {
                                foundParen = true
                                break
                            }
                            is StackToken.OperatorToken -> output.add(RpnToken.OperatorToken(top.operator))
                            is StackToken.FunctionToken -> return EngineResult.Fail(CalculatorError.Syntax)
                        }
                    }
                    if (!foundParen) return EngineResult.Fail(CalculatorError.Syntax)
                    val context = parenStack.popLastOrNull() ?: return EngineResult.Fail(CalculatorError.Syntax)
                    if (context.isFunction) {
                        val functionToken = operatorStack.popLastOrNull() as? StackToken.FunctionToken
                            ?: return EngineResult.Fail(CalculatorError.Syntax)
                        if (context.function == null) return EngineResult.Fail(CalculatorError.Syntax)
                        if (context.argCount < context.function.minArgs || context.argCount > context.function.maxArgs) {
                            return EngineResult.Fail(CalculatorError.Syntax)
                        }
                        output.add(RpnToken.FunctionCall(functionToken.function, context.argCount))
                    }
                    expectingOperand = false
                    pendingFunctionParen = false
                }

                InputToken.Comma -> {
                    val context = parenStack.lastOrNull() ?: return EngineResult.Fail(CalculatorError.Syntax)
                    if (!context.isFunction) return EngineResult.Fail(CalculatorError.Syntax)
                    if (expectingOperand) return EngineResult.Fail(CalculatorError.Syntax)
                    while (operatorStack.lastOrNull() !is StackToken.LeftParen) {
                        val popped = operatorStack.popLastOrNull() ?: return EngineResult.Fail(CalculatorError.Syntax)
                        when (popped) {
                            is StackToken.OperatorToken -> output.add(RpnToken.OperatorToken(popped.operator))
                            else -> return EngineResult.Fail(CalculatorError.Syntax)
                        }
                    }
                    context.argCount += 1
                    expectingOperand = true
                    pendingFunctionParen = false
                }

                is InputToken.OperatorSymbol -> {
                    val isUnary = expectingOperand && token.symbol != RawOperator.FACTORIAL
                    val operator = Operator.fromRaw(token.symbol, isUnary) ?: return EngineResult.Fail(CalculatorError.Syntax)
                    when (operator.position) {
                        OperatorPosition.POSTFIX -> {
                            if (expectingOperand) return EngineResult.Fail(CalculatorError.Syntax)
                        }
                        OperatorPosition.INFIX -> {
                            if (expectingOperand) return EngineResult.Fail(CalculatorError.Syntax)
                        }
                        OperatorPosition.PREFIX -> {
                            if (!expectingOperand) return EngineResult.Fail(CalculatorError.Syntax)
                        }
                    }

                    while (operatorStack.lastOrNull() is StackToken.OperatorToken) {
                        val top = (operatorStack.last() as StackToken.OperatorToken).operator
                        val shouldPop = when (operator.associativity) {
                            Associativity.LEFT -> operator.precedence <= top.precedence
                            Associativity.RIGHT -> operator.precedence < top.precedence
                        }
                        if (shouldPop) {
                            output.add(RpnToken.OperatorToken(top))
                            operatorStack.popLast()
                        } else {
                            break
                        }
                    }

                    operatorStack.add(StackToken.OperatorToken(operator))
                    pendingFunctionParen = false
                    expectingOperand = operator.position != OperatorPosition.POSTFIX
                }
            }
        }

        if (pendingFunctionParen) return EngineResult.Fail(CalculatorError.Syntax)
        if (expectingOperand) return EngineResult.Fail(CalculatorError.Syntax)

        while (operatorStack.isNotEmpty()) {
            when (val top = operatorStack.popLast()) {
                is StackToken.OperatorToken -> output.add(RpnToken.OperatorToken(top.operator))
                is StackToken.LeftParen -> return EngineResult.Fail(CalculatorError.Syntax)
                is StackToken.FunctionToken -> return EngineResult.Fail(CalculatorError.Syntax)
            }
        }

        return EngineResult.Ok(output)
    }
}

private fun <T> MutableList<T>.popLast(): T = removeAt(lastIndex)
private fun <T> MutableList<T>.popLastOrNull(): T? = if (isEmpty()) null else removeAt(lastIndex)

