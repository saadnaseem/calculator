package com.example.calculator.engine

object CalculatorEngine {

    fun evaluate(
        expression: String,
        angleMode: AngleMode = AngleMode.DEG,
        lastAnswer: Double = 0.0
    ): EvaluationResult {
        val tokens = Tokenizer.tokenize(expression)
        val rpn = tokens.flatMap { ParserShuntingYard.toRpn(it) }
        val value = rpn.flatMap { Evaluator.evaluate(it, angleMode, lastAnswer) }

        return when (value) {
            is EngineResult.Ok -> EvaluationResult.Success(value.value, Formatter.format(value.value))
            is EngineResult.Fail -> EvaluationResult.Error(value.error)
        }
    }
}
