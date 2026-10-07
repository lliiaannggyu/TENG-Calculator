package com.tengwear.jisuanqi.logic

import kotlin.math.PI
import kotlin.math.E
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * 数学表达式求值器
 *
 * 支持：
 * - 变量：x（单变量）/ x, y, z（多变量）
 * - 常量：pi, e
 * - 运算符：+ - * / ^
 * - 函数：sin cos tan asin acos atan ln log sqrt abs
 * - 隐式乘法：5x → 5*x；2(x+1) → 2*(x+1)；(x+1)(x-1) → (x+1)*(x-1)
 */
object ExpressionEvaluator {

    /** 单变量版本（绘图 / 单方程） */
    fun evaluate(expr: String, x: Double): Double? =
        evaluateVars(expr, mapOf("x" to x))

    /** 多变量版本（方程组） */
    fun evaluateVars(expr: String, vars: Map<String, Double>): Double? {
        return try {
            val cleaned = normalize(expr)
            if (cleaned.isEmpty()) return null
            val parser = Parser(cleaned, vars)
            val result = parser.parseExpression()
            if (parser.hasMore()) return null
            if (result.isNaN() || result.isInfinite()) null else result
        } catch (e: Exception) {
            null
        }
    }

    /** 隐式乘法归一化 */
    fun normalize(input: String): String {
        val src = input.replace(" ", "")
            .replace("\u00D7", "*")
            .replace("\u00F7", "/")
            .replace("\u2212", "-")
            .replace("\uFF08", "(")
            .replace("\uFF09", ")")

        if (src.isEmpty()) return src

        val sb = StringBuilder()
        var i = 0
        while (i < src.length) {
            val c = src[i]
            sb.append(c)

            if (c.isDigit() || c == '.') {
                while (i + 1 < src.length && (src[i + 1].isDigit() || src[i + 1] == '.')) {
                    i++
                    sb.append(src[i])
                }
                if (i + 1 < src.length) {
                    val next = src[i + 1]
                    if (next.isLetter() || next == '(') sb.append('*')
                }
            } else if (c == ')') {
                if (i + 1 < src.length) {
                    val next = src[i + 1]
                    if (next.isDigit() || next.isLetter() || next == '(' || next == '.') {
                        sb.append('*')
                    }
                }
            }
            i++
        }
        return sb.toString()
    }

    private class Parser(val src: String, val vars: Map<String, Double>) {
        var pos = 0

        fun hasMore(): Boolean = pos < src.length

        fun parseExpression(): Double {
            var left = parseTerm()
            while (pos < src.length && (src[pos] == '+' || src[pos] == '-')) {
                val op = src[pos++]
                val right = parseTerm()
                left = if (op == '+') left + right else left - right
            }
            return left
        }

        fun parseTerm(): Double {
            var left = parseUnary()
            while (pos < src.length && (src[pos] == '*' || src[pos] == '/')) {
                val op = src[pos++]
                val right = parseUnary()
                left = if (op == '*') left * right else left / right
            }
            return left
        }

        fun parseUnary(): Double {
            if (pos < src.length && src[pos] == '-') {
                pos++
                return -parseUnary()
            }
            if (pos < src.length && src[pos] == '+') pos++
            return parsePower()
        }

        fun parsePower(): Double {
            val base = parsePrimary()
            if (pos < src.length && src[pos] == '^') {
                pos++
                val exp = parseUnary()
                return base.pow(exp)
            }
            return base
        }

        fun parsePrimary(): Double {
            if (pos < src.length && src[pos] == '(') {
                pos++
                val v = parseExpression()
                if (pos < src.length && src[pos] == ')') pos++
                return v
            }

            if (pos < src.length && (src[pos].isDigit() || src[pos] == '.')) {
                val start = pos
                while (pos < src.length && (src[pos].isDigit() || src[pos] == '.')) pos++
                return src.substring(start, pos).toDouble()
            }

            if (pos < src.length && src[pos].isLetter()) {
                val start = pos
                while (pos < src.length && src[pos].isLetterOrDigit()) pos++
                val name = src.substring(start, pos).lowercase()

                when (name) {
                    "pi" -> return PI
                    "e" -> return E
                }

                if (pos < src.length && src[pos] == '(') {
                    pos++
                    val arg = parseExpression()
                    if (pos < src.length && src[pos] == ')') pos++
                    return applyFunction(name, arg)
                }

                // 变量
                vars[name]?.let { return it }

                throw IllegalArgumentException("未知标识符: $name")
            }

            throw IllegalArgumentException("意外的字符: pos=$pos")
        }

        private fun applyFunction(name: String, arg: Double): Double {
            return when (name) {
                "sin" -> sin(arg)
                "cos" -> cos(arg)
                "tan" -> tan(arg)
                "asin" -> asin(arg)
                "acos" -> acos(arg)
                "atan" -> atan(arg)
                "ln" -> ln(arg)
                "log" -> log10(arg)
                "sqrt" -> sqrt(arg)
                "abs" -> abs(arg)
                else -> throw IllegalArgumentException("未知函数: $name")
            }
        }
    }
}