package com.example.calculator.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTests {

    private fun assertSuccess(
        expression: String,
        expected: String,
        angleMode: AngleMode = AngleMode.DEG,
        lastAns: Double = 0.0
    ) {
        val result = CalculatorEngine.evaluate(expression, angleMode, lastAns)
        assertTrue("Expected success for $expression but got $result", result is EvaluationResult.Success)
        result as EvaluationResult.Success
        assertEquals("Mismatch for $expression", expected, result.formatted)
    }

    private fun assertError(
        expression: String,
        angleMode: AngleMode = AngleMode.DEG,
        lastAns: Double = 0.0
    ) {
        val result = CalculatorEngine.evaluate(expression, angleMode, lastAns)
        assertTrue("Expected error for $expression", result is EvaluationResult.Error)
    }

    @Test
    fun specExamples() {
        assertSuccess("1+2*3", "7")
        assertSuccess("(1+2)*3", "9")
        assertSuccess("2^3^2", "512")
        assertSuccess("-3^2", "-9")
        assertSuccess("(-3)^2", "9")
        assertSuccess("5!", "120")
        assertSuccess("0!", "1")
        assertError("3.2!")
        assertSuccess("sin(30)", "0.5")
        assertSuccess("cos(0)", "1")
        assertSuccess("tan(45)", "1")
        assertSuccess("asin(0.5)", "30")
        assertSuccess("acos(1)", "0")
        assertSuccess("atan(1)", "45")
        assertSuccess("ln(e)", "1")
        assertSuccess("log(1000)", "3")
        assertSuccess("log(8,2)", "3")
        assertSuccess("sqrt(9)", "3")
        assertError("sqrt(-1)")
        assertSuccess("abs(-3.5)", "3.5")
        assertSuccess("exp(1)", "2.71828182846")
        assertSuccess("1/3", "0.333333333333")
        assertError("2/0")
        assertSuccess("(2+3)*(4-1)", "15")
        assertSuccess("π+e", "5.85987448205")
        assertSuccess("-0.0000004", "-4e-7")
        assertSuccess("10000000000", "1e10")
        assertSuccess("ANS+5", "5", lastAns = 0.0)
        assertSuccess("ANS*3", "12", lastAns = 4.0)
        assertError("sin(30)!")
        assertSuccess("sqrt(abs(-16))", "4")
        assertSuccess("log(1,10)", "0")
        assertError("log(1,1)")
        assertError("tan(90)")
        assertError("asin(2)")
    }
}

