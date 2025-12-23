package com.example.calculator.engine

enum class AngleMode { DEG, RAD }

sealed class CalculatorError {
    object Syntax : CalculatorError()
    object Math : CalculatorError()
}

sealed class EvaluationResult {
    data class Success(val value: Double, val formatted: String) : EvaluationResult()
    data class Error(val error: CalculatorError) : EvaluationResult()
}

sealed class EngineResult<out T> {
    data class Ok<T>(val value: T) : EngineResult<T>()
    data class Fail(val error: CalculatorError) : EngineResult<Nothing>()
}

inline fun <T, R> EngineResult<T>.map(transform: (T) -> R): EngineResult<R> =
    when (this) {
        is EngineResult.Ok -> EngineResult.Ok(transform(value))
        is EngineResult.Fail -> this
    }

inline fun <T, R> EngineResult<T>.flatMap(transform: (T) -> EngineResult<R>): EngineResult<R> =
    when (this) {
        is EngineResult.Ok -> transform(value)
        is EngineResult.Fail -> this
    }

