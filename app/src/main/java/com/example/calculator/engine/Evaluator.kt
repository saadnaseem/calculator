package com.example.calculator.engine

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

private const val ZERO_EPSILON = 1e-12

object Evaluator {

    fun evaluate(
        rpn: List<RpnToken>,
        angleMode: AngleMode,
        lastAnswer: Double
    ): EngineResult<Double> {
        val stack = ArrayDeque<Double>()

        for (token in rpn) {
            when (token) {
                is RpnToken.Number -> stack.addLast(token.value)
                RpnToken.Ans -> stack.addLast(lastAnswer)
                is RpnToken.OperatorToken -> {
                    val operator = token.operator
                    if (stack.size < operator.arity) return EngineResult.Fail(CalculatorError.Syntax)
                    val result = when (operator.arity) {
                        1 -> applyUnary(operator.type, stack.removeLast())
                        2 -> {
                            val right = stack.removeLast()
                            val left = stack.removeLast()
                            applyBinary(operator.type, left, right)
                        }

                        else -> EngineResult.Fail(CalculatorError.Syntax)
                    }
                    when (result) {
                        is EngineResult.Ok -> stack.addLast(result.value)
                        is EngineResult.Fail -> return result
                    }
                }

                is RpnToken.FunctionCall -> {
                    if (stack.size < token.argCount) return EngineResult.Fail(CalculatorError.Syntax)
                    val args = List(token.argCount) { stack.removeLast() }.reversed()
                    val valueResult = applyFunction(token.function, args, angleMode)
                    when (valueResult) {
                        is EngineResult.Ok -> stack.addLast(valueResult.value)
                        is EngineResult.Fail -> return valueResult
                    }
                }
            }
        }

        return if (stack.size == 1) {
            EngineResult.Ok(stack.last())
        } else {
            EngineResult.Fail(CalculatorError.Syntax)
        }
    }

    private fun applyUnary(type: OperatorType, operand: Double): EngineResult<Double> {
        return when (type) {
            OperatorType.UNARY_MINUS -> EngineResult.Ok(-operand)
            OperatorType.FACTORIAL -> factorial(operand)
            else -> EngineResult.Fail(CalculatorError.Syntax)
        }
    }

    private fun applyBinary(type: OperatorType, left: Double, right: Double): EngineResult<Double> {
        val result = when (type) {
            OperatorType.ADD -> left + right
            OperatorType.SUBTRACT -> left - right
            OperatorType.MULTIPLY -> left * right
            OperatorType.DIVIDE -> {
                if (abs(right) < ZERO_EPSILON) return EngineResult.Fail(CalculatorError.Math)
                left / right
            }

            OperatorType.POWER -> left.pow(right)
            else -> return EngineResult.Fail(CalculatorError.Syntax)
        }
        return if (result.isFinite()) EngineResult.Ok(result) else EngineResult.Fail(CalculatorError.Math)
    }

    private fun applyFunction(
        type: FunctionType,
        args: List<Double>,
        angleMode: AngleMode
    ): EngineResult<Double> {
        val value = when (type) {
            FunctionType.SIN -> {
                val rad = toRadians(angleMode, args[0])
                sin(rad)
            }

            FunctionType.COS -> {
                val rad = toRadians(angleMode, args[0])
                cos(rad)
            }

            FunctionType.TAN -> {
                val rad = toRadians(angleMode, args[0])
                val cosValue = cos(rad)
                if (abs(cosValue) < ZERO_EPSILON) return EngineResult.Fail(CalculatorError.Math)
                tan(rad)
            }

            FunctionType.ASIN -> {
                val input = args[0]
                if (input < -1 || input > 1) return EngineResult.Fail(CalculatorError.Math)
                val rad = asin(input)
                fromRadians(angleMode, rad)
            }

            FunctionType.ACOS -> {
                val input = args[0]
                if (input < -1 || input > 1) return EngineResult.Fail(CalculatorError.Math)
                val rad = acos(input)
                fromRadians(angleMode, rad)
            }

            FunctionType.ATAN -> {
                val rad = atan(args[0])
                fromRadians(angleMode, rad)
            }

            FunctionType.LN -> {
                val input = args[0]
                if (input <= 0) return EngineResult.Fail(CalculatorError.Math)
                ln(input)
            }

            FunctionType.LOG -> {
                if (args.size == 1) {
                    val input = args[0]
                    if (input <= 0) return EngineResult.Fail(CalculatorError.Math)
                    log10(input)
                } else {
                    val input = args[0]
                    val base = args[1]
                    if (input <= 0 || base <= 0 || abs(base - 1.0) < ZERO_EPSILON) {
                        return EngineResult.Fail(CalculatorError.Math)
                    }
                    ln(input) / ln(base)
                }
            }

            FunctionType.SQRT -> {
                val input = args[0]
                if (input < 0) return EngineResult.Fail(CalculatorError.Math)
                sqrt(input)
            }

            FunctionType.ABS -> abs(args[0])
            FunctionType.EXP -> exp(args[0])
        }

        return if (value.isFinite()) EngineResult.Ok(value) else EngineResult.Fail(CalculatorError.Math)
    }

    private fun factorial(value: Double): EngineResult<Double> {
        if (!value.isFinite()) return EngineResult.Fail(CalculatorError.Math)
        if (value < 0) return EngineResult.Fail(CalculatorError.Math)
        val rounded = value.toLong()
        if (abs(value - rounded) > ZERO_EPSILON) return EngineResult.Fail(CalculatorError.Math)
        if (rounded > 170) return EngineResult.Fail(CalculatorError.Math)
        var result = 1.0
        for (i in 2..rounded) {
            result *= i
            if (!result.isFinite()) return EngineResult.Fail(CalculatorError.Math)
        }
        return EngineResult.Ok(result)
    }

    private fun toRadians(mode: AngleMode, value: Double): Double =
        if (mode == AngleMode.DEG) Math.toRadians(value) else value

    private fun fromRadians(mode: AngleMode, value: Double): Double =
        if (mode == AngleMode.DEG) Math.toDegrees(value) else value
}

