package com.example.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

class ExpressionEvaluator(var isDegreeMode: Boolean = true) {

    data class EvalResult(
        val value: Double,
        val formatted: String,
        val isError: Boolean = false,
        val errorMessage: String = ""
    )

    fun evaluate(expression: String): EvalResult {
        if (expression.isBlank()) {
            return EvalResult(0.0, "0")
        }

        return try {
            val sanitized = sanitizeExpression(expression)
            val tokens = tokenize(sanitized)
            val postfix = shuntingYard(tokens)
            val result = evaluatePostfix(postfix)

            if (result.isNaN()) {
                EvalResult(Double.NaN, "Undefined", isError = true, errorMessage = "Result is undefined")
            } else if (result.isInfinite()) {
                EvalResult(Double.POSITIVE_INFINITY, if (result > 0) "Infinity" else "-Infinity", isError = true, errorMessage = "Cannot divide by zero")
            } else {
                EvalResult(result, formatResult(result))
            }
        } catch (e: Exception) {
            EvalResult(0.0, "", isError = true, errorMessage = e.message ?: "Invalid Expression")
        }
    }

    fun quickPreview(expression: String): String? {
        if (expression.isBlank() || expression.all { it.isDigit() || it == '.' || it == ' ' }) {
            return null
        }
        val res = evaluate(expression)
        return if (!res.isError && res.formatted.isNotBlank()) res.formatted else null
    }

    private fun sanitizeExpression(raw: String): String {
        var expr = raw
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "${Math.PI}")
            .replace("e", "${Math.E}")
            .replace(" ", "")

        // Handle implicit multiplication: digit followed by '(', digit followed by function, ')' followed by '(', etc.
        val implicitMultiplyPattern = Regex("(?<=[0-9πe\\)])(?=[a-df-z√(])|(?<=[0-9πe])(?=[πe])")
        expr = expr.replace(implicitMultiplyPattern, "*")

        // Balance parentheses
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        if (openCount > closeCount) {
            expr += ")".repeat(openCount - closeCount)
        }

        return expr
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        var expectUnary = true

        while (i < expr.length) {
            val ch = expr[i]

            // Numbers
            if (ch.isDigit() || ch == '.') {
                val sb = StringBuilder()
                while (i < expr.length && (expr[i].isDigit() || expr[i] == '.' || expr[i] == 'E' || (expr[i] == '+' || expr[i] == '-') && i > 0 && expr[i - 1] == 'E')) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
                expectUnary = false
                continue
            }

            // Unary minus
            if (ch == '-' && expectUnary) {
                // Check if negative number
                tokens.add("neg")
                i++
                continue
            }

            // Functions: sin, cos, tan, asin, acos, atan, ln, log, sqrt, cbrt, abs
            if (ch.isLetter() || ch == '√' || ch == '∛') {
                val sb = StringBuilder()
                while (i < expr.length && (expr[i].isLetter() || expr[i] == '√' || expr[i] == '∛' || expr[i] == '1' || expr[i] == '0')) {
                    sb.append(expr[i])
                    i++
                }
                val name = sb.toString()
                tokens.add(name)
                expectUnary = false
                continue
            }

            // Operators and brackets
            when (ch) {
                '+', '-', '*', '/', '^', '%', '!' -> {
                    tokens.add(ch.toString())
                    expectUnary = ch != '!'
                }
                '(' -> {
                    tokens.add("(")
                    expectUnary = true
                }
                ')' -> {
                    tokens.add(")")
                    expectUnary = false
                }
                else -> {
                    // Ignore unrecognized whitespace or glyphs
                }
            }
            i++
        }
        return tokens
    }

    private fun precedence(op: String): Int = when (op) {
        "!" -> 5
        "neg" -> 4
        "^" -> 4
        "*", "/", "%" -> 3
        "+", "-" -> 2
        else -> 1
    }

    private fun isRightAssociative(op: String): Boolean = op == "^" || op == "neg"

    private fun isFunction(token: String): Boolean = token in setOf(
        "sin", "cos", "tan", "asin", "acos", "atan",
        "ln", "log", "sqrt", "√", "cbrt", "∛", "abs"
    )

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                output.add(token)
            } else if (isFunction(token)) {
                stack.addLast(token)
            } else if (token == "(") {
                stack.addLast(token)
            } else if (token == ")") {
                while (stack.isNotEmpty() && stack.last() != "(") {
                    output.add(stack.removeLast())
                }
                if (stack.isNotEmpty() && stack.last() == "(") {
                    stack.removeLast()
                }
                if (stack.isNotEmpty() && isFunction(stack.last())) {
                    output.add(stack.removeLast())
                }
            } else {
                // Operator
                val p1 = precedence(token)
                while (stack.isNotEmpty() && stack.last() != "(") {
                    val top = stack.last()
                    val p2 = precedence(top)
                    if (p2 > p1 || (p2 == p1 && !isRightAssociative(token))) {
                        output.add(stack.removeLast())
                    } else {
                        break
                    }
                }
                stack.addLast(token)
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top != "(" && top != ")") {
                output.add(top)
            }
        }

        return output
    }

    private fun evaluatePostfix(postfix: List<String>): Double {
        val stack = ArrayDeque<Double>()

        for (token in postfix) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.addLast(num)
                continue
            }

            if (isFunction(token)) {
                val a = if (stack.isNotEmpty()) stack.removeLast() else 0.0
                val res = when (token) {
                    "sin" -> {
                        val angle = if (isDegreeMode) Math.toRadians(a) else a
                        cleanTrig(sin(angle), angle)
                    }
                    "cos" -> {
                        val angle = if (isDegreeMode) Math.toRadians(a) else a
                        cleanTrig(cos(angle), angle)
                    }
                    "tan" -> {
                        val angle = if (isDegreeMode) Math.toRadians(a) else a
                        val c = cos(angle)
                        if (abs(c) < 1e-12) Double.NaN else cleanTrig(tan(angle), angle)
                    }
                    "asin" -> {
                        val rad = asin(a)
                        if (isDegreeMode) Math.toDegrees(rad) else rad
                    }
                    "acos" -> {
                        val rad = acos(a)
                        if (isDegreeMode) Math.toDegrees(rad) else rad
                    }
                    "atan" -> {
                        val rad = atan(a)
                        if (isDegreeMode) Math.toDegrees(rad) else rad
                    }
                    "ln" -> if (a <= 0) Double.NaN else ln(a)
                    "log" -> if (a <= 0) Double.NaN else log10(a)
                    "sqrt", "√" -> if (a < 0) Double.NaN else sqrt(a)
                    "cbrt", "∛" -> cbrt(a)
                    "abs" -> abs(a)
                    else -> a
                }
                stack.addLast(res)
                continue
            }

            if (token == "neg") {
                val a = if (stack.isNotEmpty()) stack.removeLast() else 0.0
                stack.addLast(-a)
                continue
            }

            if (token == "!") {
                val a = if (stack.isNotEmpty()) stack.removeLast() else 0.0
                stack.addLast(factorial(a))
                continue
            }

            // Binary operators
            val b = if (stack.isNotEmpty()) stack.removeLast() else 0.0
            val a = if (stack.isNotEmpty()) stack.removeLast() else 0.0

            val result = when (token) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> if (b == 0.0) Double.POSITIVE_INFINITY else a / b
                "%" -> (a * b) / 100.0 // Standard calculator percentage of 'a'
                "^" -> a.pow(b)
                else -> 0.0
            }
            stack.addLast(result)
        }

        return if (stack.isNotEmpty()) stack.last() else 0.0
    }

    private fun cleanTrig(value: Double, angleRad: Double): Double {
        return if (abs(value) < 1e-12) 0.0 else if (abs(value - 1.0) < 1e-12) 1.0 else if (abs(value + 1.0) < 1e-12) -1.0 else value
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n) || n > 170) return Double.NaN
        var res = 1.0
        for (i in 2..n.toInt()) {
            res *= i
        }
        return res
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "Undefined"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // Avoid floating precision noise (e.g. 0.30000000000000004 -> 0.3)
        val bd = try {
            BigDecimal(value, MathContext(12, RoundingMode.HALF_UP)).stripTrailingZeros()
        } catch (e: Exception) {
            BigDecimal(value)
        }

        val absVal = abs(value)
        return if ((absVal >= 1e12 || (absVal < 1e-6 && absVal > 0))) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val formatter = DecimalFormat("0.######E0", symbols)
            formatter.format(value)
        } else {
            val symbols = DecimalFormatSymbols(Locale.US).apply {
                groupingSeparator = ','
                decimalSeparator = '.'
            }
            val plain = bd.toPlainString()
            if (plain.contains('.')) {
                val parts = plain.split('.')
                val intPart = parts[0].toLongOrNull()
                if (intPart != null) {
                    val intFormatted = DecimalFormat("#,###", symbols).format(intPart)
                    "$intFormatted.${parts[1]}"
                } else {
                    plain
                }
            } else {
                val intPart = plain.toLongOrNull()
                if (intPart != null) {
                    DecimalFormat("#,###", symbols).format(intPart)
                } else {
                    plain
                }
            }
        }
    }
}
