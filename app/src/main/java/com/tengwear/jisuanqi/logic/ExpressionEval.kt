package com.tengwear.jisuanqi.logic

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * 表达式求值器。
 *
 * 支持：
 *   + - * / ^ √   （√ 为二元：a √ b = a 的 b 次方根）
 *   ( )  一元负号
 *
 * 内部用 Rational 精确计算；无理部分（非整数指数、非完全根）转近似。
 * 优先级：^  >  一元负号 > √ * / > + -
 */
object ExpressionEval {

    data class Result(val value: Rational, val isApprox: Boolean)

    fun eval(input: String, onProgress: (Float) -> Unit = {}): Result? {
        return try {
            onProgress(0.05f)
            val tokens = tokenize(input)
            if (tokens.isEmpty()) return null
            val parser = Parser(tokens, onProgress)
            val r = parser.parseExpr()
            if (!parser.atEnd()) return null
            onProgress(1f)
            r
        } catch (_: Exception) {
            null
        }
    }

    // ============ 词法 ============

    private sealed interface Token
    private data class NumTok(val r: Rational) : Token
    private data class OpTok(val op: String) : Token   // + - * / ^ √
    private data object LParen : Token
    private data object RParen : Token

    private fun tokenize(s: String): List<Token> {
        val out = mutableListOf<Token>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || c == '.' -> {
                    val start = i
                    while (i < s.length && (s[i].isDigit() || s[i] == '.')) i++
                    val str = s.substring(start, i)
                    val r = Rational.parse(str) ?: throw IllegalArgumentException("bad num")
                    out.add(NumTok(r))
                }
                c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == '√' -> {
                    out.add(OpTok(c.toString())); i++
                }
                c == '×' -> { out.add(OpTok("*")); i++ }
                c == '÷' -> { out.add(OpTok("/")); i++ }
                c == '(' -> { out.add(LParen); i++ }
                c == ')' -> { out.add(RParen); i++ }
                else -> throw IllegalArgumentException("bad char: $c")
            }
        }
        return out
    }

    // ============ 语法 ============

    private class Parser(
        val tokens: List<Token>,
        val onProgress: (Float) -> Unit
    ) {
        var idx = 0
        var isApprox = false

        fun atEnd() = idx >= tokens.size
        private fun peek() = tokens.getOrNull(idx)

        // expr := term (('+' | '-') term)*
        fun parseExpr(): Result {
            var acc = parseTerm()
            while (true) {
                val t = peek()
                if (t is OpTok && (t.op == "+" || t.op == "-")) {
                    idx++
                    val right = parseTerm()
                    val v = if (t.op == "+") acc.value + right.value else acc.value - right.value
                    acc = Result(v, acc.isApprox || right.isApprox)
                } else break
            }
            return acc
        }

        // term := unary (('*' | '/' | '√') unary)*
        fun parseTerm(): Result {
            var acc = parseUnary()
            while (true) {
                val t = peek()
                if (t is OpTok && (t.op == "*" || t.op == "/" || t.op == "√")) {
                    idx++
                    val right = parseUnary()
                    acc = when (t.op) {
                        "*" -> Result(acc.value * right.value, acc.isApprox || right.isApprox)
                        "/" -> {
                            if (right.value.isZero()) throw ArithmeticException()
                            Result(acc.value / right.value, acc.isApprox || right.isApprox)
                        }
                        "√" -> nthRoot(acc.value, right.value)
                        else -> throw ArithmeticException()
                    }
                } else break
            }
            return acc
        }

        // unary := ('-' | '+')? power
        fun parseUnary(): Result {
            val t = peek()
            if (t is OpTok && (t.op == "-" || t.op == "+")) {
                idx++
                val r = parseUnary()
                return if (t.op == "-") Result(r.value.negate(), r.isApprox) else r
            }
            return parsePower()
        }

        // power := primary ('^' unary)?   右结合
        fun parsePower(): Result {
            val base = parsePrimary()
            val t = peek()
            if (t is OpTok && t.op == "^") {
                idx++
                val exp = parseUnary()
                return powExact(base.value, exp.value)
            }
            return base
        }

        // primary := number | '(' expr ')'
        fun parsePrimary(): Result {
            val t = peek() ?: throw ArithmeticException()
            return when (t) {
                is NumTok -> { idx++; Result(t.r, false) }
                LParen -> {
                    idx++
                    val r = parseExpr()
                    val close = peek()
                    if (close !is RParen) throw ArithmeticException()
                    idx++
                    r
                }
                else -> throw ArithmeticException()
            }
        }

        // ---- 具体运算 ----

        private fun nthRoot(a: Rational, n: Rational): Result {
            if (!n.isInteger()) throw ArithmeticException()
            val nBI = n.toBigIntegerExact()
            if (nBI.signum() <= 0 || nBI > java.math.BigInteger.valueOf(1_000_000L)) {
                throw ArithmeticException()
            }
            val ni = nBI.toInt()
            a.nthRootExact(ni)?.let { return Result(it, false) }
            if (a.signum() < 0 && ni % 2 == 0) throw ArithmeticException()

            onProgress(0.3f)
            val d = Math.pow(a.abs().toDouble(), 1.0 / ni)
            if (d.isNaN() || !d.isFinite()) throw ArithmeticException()
            onProgress(0.8f)
            val signed = if (a.signum() < 0) -d else d
            val s = formatDouble(signed)
            val r = Rational.parse(s) ?: throw ArithmeticException()
            return Result(r, true)
        }

        private fun powExact(base: Rational, exp: Rational): Result {
            if (!exp.isInteger()) {
                // 非整数指数：近似
                onProgress(0.3f)
                val d = Math.pow(base.toDouble(), exp.toDouble())
                if (d.isNaN() || !d.isFinite()) throw ArithmeticException()
                onProgress(0.8f)
                val s = formatDouble(d)
                val r = Rational.parse(s) ?: throw ArithmeticException()
                return Result(r, true)
            }
            val nBI = exp.toBigIntegerExact()
            if (nBI.abs() > java.math.BigInteger.valueOf(Int.MAX_VALUE.toLong())) {
                throw ArithmeticException()
            }
            onProgress(0.3f)
            val r = base.pow(nBI.toInt())
            onProgress(0.95f)
            return Result(r, false)
        }

        private fun formatDouble(d: Double): String {
            return BigDecimal(d).setScale(15, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
        }
    }
}