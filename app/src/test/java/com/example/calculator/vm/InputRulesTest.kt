package com.example.calculator.vm

import org.junit.Assert.assertEquals
import org.junit.Test

class InputRulesTest {

    @Test
    fun autoInsertsAnsForLeadingOperator() {
        assertEquals("ANS+", applyInputRules("", "+"))
    }

    @Test
    fun autoInsertsAnsForLeadingFunction() {
        assertEquals("sin(", applyInputRules("", "sin("))
    }

    @Test
    fun appendsNormallyWhenExpressionExists() {
        assertEquals("12+", applyInputRules("12", "+"))
    }

    @Test
    fun smartBackspaceRemovesAutoFilledAns() {
        assertEquals("sin(", smartBackspace("sin(ANS)"))
    }

    @Test
    fun smartBackspaceHandlesTokens() {
        assertEquals("", smartBackspace("sin("))
        assertEquals("", smartBackspace("ANS"))
        assertEquals("123", smartBackspace("1234"))
    }
}


