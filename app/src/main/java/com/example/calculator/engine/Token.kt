package com.example.calculator.engine

enum class Associativity { LEFT, RIGHT }

enum class OperatorPosition { PREFIX, INFIX, POSTFIX }

enum class RawOperator {
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    POWER,
    FACTORIAL
}

enum class FunctionType(val display: String, val minArgs: Int = 1, val maxArgs: Int = 1) {
    SIN("sin"),
    COS("cos"),
    TAN("tan"),
    ASIN("asin"),
    ACOS("acos"),
    ATAN("atan"),
    LN("ln"),
    LOG("log", minArgs = 1, maxArgs = 2),
    SQRT("sqrt"),
    ABS("abs"),
    EXP("exp");

    companion object {
        fun fromName(name: String): FunctionType? =
            entries.firstOrNull { it.display == name }
    }
}

enum class ConstantType(val value: Double) {
    PI(kotlin.math.PI),
    E(kotlin.math.E);
}

data class Operator(
    val type: OperatorType,
    val precedence: Int,
    val associativity: Associativity,
    val position: OperatorPosition,
    val arity: Int
) {
    companion object {
        fun fromRaw(raw: RawOperator, isUnary: Boolean): Operator? {
            return when (raw) {
                RawOperator.PLUS -> if (isUnary) null else OperatorType.ADD.operator()
                RawOperator.MINUS -> if (isUnary) OperatorType.UNARY_MINUS.operator() else OperatorType.SUBTRACT.operator()
                RawOperator.MULTIPLY -> if (isUnary) null else OperatorType.MULTIPLY.operator()
                RawOperator.DIVIDE -> if (isUnary) null else OperatorType.DIVIDE.operator()
                RawOperator.POWER -> if (isUnary) null else OperatorType.POWER.operator()
                RawOperator.FACTORIAL -> if (isUnary) null else OperatorType.FACTORIAL.operator()
            }
        }
    }
}

enum class OperatorType {
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    POWER,
    UNARY_MINUS,
    FACTORIAL;

    fun operator(): Operator {
        return when (this) {
            ADD -> Operator(this, precedence = 1, associativity = Associativity.LEFT, position = OperatorPosition.INFIX, arity = 2)
            SUBTRACT -> Operator(this, precedence = 1, associativity = Associativity.LEFT, position = OperatorPosition.INFIX, arity = 2)
            MULTIPLY -> Operator(this, precedence = 2, associativity = Associativity.LEFT, position = OperatorPosition.INFIX, arity = 2)
            DIVIDE -> Operator(this, precedence = 2, associativity = Associativity.LEFT, position = OperatorPosition.INFIX, arity = 2)
            POWER -> Operator(this, precedence = 4, associativity = Associativity.RIGHT, position = OperatorPosition.INFIX, arity = 2)
            UNARY_MINUS -> Operator(this, precedence = 3, associativity = Associativity.RIGHT, position = OperatorPosition.PREFIX, arity = 1)
            FACTORIAL -> Operator(this, precedence = 5, associativity = Associativity.LEFT, position = OperatorPosition.POSTFIX, arity = 1)
        }
    }
}

sealed class InputToken {
    data class Number(val value: Double) : InputToken()
    data class OperatorSymbol(val symbol: RawOperator) : InputToken()
    data class FunctionName(val function: FunctionType) : InputToken()
    data class Constant(val constant: ConstantType) : InputToken()
    data object Ans : InputToken()
    data object LeftParen : InputToken()
    data object RightParen : InputToken()
    data object Comma : InputToken()
}

sealed class RpnToken {
    data class Number(val value: Double) : RpnToken()
    data object Ans : RpnToken()
    data class OperatorToken(val operator: Operator) : RpnToken()
    data class FunctionCall(val function: FunctionType, val argCount: Int) : RpnToken()
}

