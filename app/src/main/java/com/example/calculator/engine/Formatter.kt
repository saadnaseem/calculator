package com.example.calculator.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

private const val SIGNIFICANT_DIGITS = 12
private const val SCIENTIFIC_UPPER = 1e9
private const val SCIENTIFIC_LOWER = 1e-6

object Formatter {

    fun format(value: Double): String {
        if (!value.isFinite()) return "Error"

        val adjusted = if (abs(value) < 1e-12) 0.0 else value
        val absValue = abs(adjusted)
        if (absValue == 0.0) return "0"

        val useScientific = absValue >= SCIENTIFIC_UPPER || (absValue > 0 && absValue < SCIENTIFIC_LOWER)
        val mathContext = MathContext(SIGNIFICANT_DIGITS, RoundingMode.HALF_UP)

        return if (useScientific) {
            var exponent = floor(log10(absValue)).toInt()
            var mantissa = adjusted / 10.0.pow(exponent)
            var mantissaDecimal = BigDecimal(mantissa, mathContext)
            if (mantissaDecimal.abs() >= BigDecimal.TEN) {
                mantissaDecimal = mantissaDecimal.divide(BigDecimal.TEN, mathContext)
                exponent += 1
            }
            val mantissaText = mantissaDecimal.stripTrailingZeros().toPlainString()
            "$mantissaText" + "e$exponent"
        } else {
            BigDecimal(adjusted, mathContext)
                .stripTrailingZeros()
                .toPlainString()
        }
    }
}

