package com.tengwear.jisuanqi.logic

import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToLong

data class DerivativeResult(
    val input: String,
    val derivative: String,
    val steps: List<String>,
    val error: String?
)

object DerivativeSolver {

    fun num(v: Double): String {
        val r = v.roundToLong()
        return if (abs(v - r.toDouble()) < 1e-9) r.toString()
        else {
            val s = String.format("%.4f", v)
            s.trimEnd('0').trimEnd('.')
        }
    }

    fun solve(input: String): DerivativeResult {
        val steps = ArrayList<String>()
        return try {
            val normalized = ExpressionEvaluator.normalize(input)
            if (normalized.isEmpty()) {
                return DerivativeResult(input, "", steps, "表达式为空")
            }
            val ast = Parser(normalized).parse()
            steps.add("原函数：f(x) = ${format(input)}")
            val raw = derivative(ast)
            val simplified = simplify(raw)
            val derivText = print(simplified)
            steps.add("求导结果：f\u2032(x) = $derivText")
            DerivativeResult(input, derivText, steps, null)
        } catch (e: Exception) {
            DerivativeResult(input, "", steps, "无法求导：${e.message}")
        }
    }

    // ============ AST ============
    private sealed class E {
        data class Num(val v: Double) : E()
        object Var : E()
        data class Add(val l: E, val r: E) : E()
        data class Sub(val l: E, val r: E) : E()
        data class Mul(val l: E, val r: E) : E()
        data class Div(val l: E, val r: E) : E()
        data class Pow(val l: E, val r: E) : E()
        data class Neg(val e: E) : E()
        data class Fn(val name: String, val arg: E) : E()
    }

    // ============ 解析器 ============
    private class Parser(val src: String) {
        var pos = 0

        fun parse(): E {
            val e = parseExpr()
            if (pos < src.length) throw IllegalArgumentException("多余的字符: ${src.substring(pos)}")
            return e
        }

        fun parseExpr(): E {
            var l = parseTerm()
            while (pos < src.length && (src[pos] == '+' || src[pos] == '-')) {
                val op = src[pos++]
                val r = parseTerm()
                l = if (op == '+') E.Add(l, r) else E.Sub(l, r)
            }
            return l
        }

        fun parseTerm(): E {
            var l = parseUnary()
            while (pos < src.length && (src[pos] == '*' || src[pos] == '/')) {
                val op = src[pos++]
                val r = parseUnary()
                l = if (op == '*') E.Mul(l, r) else E.Div(l, r)
            }
            return l
        }

        fun parseUnary(): E {
            if (pos < src.length && src[pos] == '-') {
                pos++
                return E.Neg(parseUnary())
            }
            if (pos < src.length && src[pos] == '+') pos++
            return parsePower()
        }

        fun parsePower(): E {
            val b = parsePrimary()
            if (pos < src.length && src[pos] == '^') {
                pos++
                val e = parseUnary()
                return E.Pow(b, e)
            }
            return b
        }

        fun parsePrimary(): E {
            if (pos < src.length && src[pos] == '(') {
                pos++
                val e = parseExpr()
                if (pos < src.length && src[pos] == ')') pos++
                return e
            }
            if (pos < src.length && (src[pos].isDigit() || src[pos] == '.')) {
                val s = pos
                while (pos < src.length && (src[pos].isDigit() || src[pos] == '.')) pos++
                return E.Num(src.substring(s, pos).toDouble())
            }
            if (pos < src.length && src[pos].isLetter()) {
                val s = pos
                while (pos < src.length && src[pos].isLetterOrDigit()) pos++
                val name = src.substring(s, pos).lowercase()
                when (name) {
                    "x" -> return E.Var
                    "pi" -> return E.Num(Math.PI)
                    "e" -> return E.Num(Math.E)
                }
                if (pos < src.length && src[pos] == '(') {
                    pos++
                    val arg = parseExpr()
                    if (pos < src.length && src[pos] == ')') pos++
                    return E.Fn(name, arg)
                }
                throw IllegalArgumentException("未知标识符: $name")
            }
            throw IllegalArgumentException("意外字符 @$pos")
        }
    }

    // ============ 求导 ============
    private fun derivative(e: E): E = when (e) {
        is E.Num -> E.Num(0.0)
        is E.Var -> E.Num(1.0)
        is E.Add -> E.Add(derivative(e.l), derivative(e.r))
        is E.Sub -> E.Sub(derivative(e.l), derivative(e.r))
        is E.Mul -> E.Add(
            E.Mul(derivative(e.l), e.r),
            E.Mul(e.l, derivative(e.r))
        )
        is E.Div -> E.Div(
            E.Sub(
                E.Mul(derivative(e.l), e.r),
                E.Mul(e.l, derivative(e.r))
            ),
            E.Pow(e.r, E.Num(2.0))
        )
        is E.Pow -> {
            if (e.r is E.Num) {
                val n = (e.r as E.Num).v
                E.Mul(
                    E.Mul(E.Num(n), E.Pow(e.l, E.Num(n - 1.0))),
                    derivative(e.l)
                )
            } else {
                E.Mul(e, E.Add(
                    E.Mul(derivative(e.r), E.Fn("ln", e.l)),
                    E.Div(E.Mul(e.r, derivative(e.l)), e.l)
                ))
            }
        }
        is E.Neg -> E.Neg(derivative(e.e))
        is E.Fn -> {
            val u = e.arg
            val du = derivative(u)
            when (e.name) {
                "sin" -> E.Mul(E.Fn("cos", u), du)
                "cos" -> E.Neg(E.Mul(E.Fn("sin", u), du))
                "tan" -> E.Div(du, E.Pow(E.Fn("cos", u), E.Num(2.0)))
                "ln" -> E.Div(du, u)
                "log" -> E.Div(du, E.Mul(u, E.Num(ln(10.0))))
                "sqrt" -> E.Div(du, E.Mul(E.Num(2.0), E.Fn("sqrt", u)))
                "abs" -> E.Div(E.Mul(u, du), E.Fn("abs", u))
                "asin" -> E.Div(du, E.Fn("sqrt",
                    E.Sub(E.Num(1.0), E.Pow(u, E.Num(2.0)))))
                "acos" -> E.Neg(E.Div(du, E.Fn("sqrt",
                    E.Sub(E.Num(1.0), E.Pow(u, E.Num(2.0))))))
                "atan" -> E.Div(du, E.Add(E.Num(1.0), E.Pow(u, E.Num(2.0))))
                "exp" -> E.Mul(E.Fn("exp", u), du)
                else -> throw IllegalArgumentException("未知函数: ${e.name}")
            }
        }
    }

    // ============ 化简 ============
    private fun simplify(e: E): E = when (e) {
        is E.Num, is E.Var -> e
        is E.Neg -> {
            val s = simplify(e.e)
            when {
                s is E.Num -> E.Num(-s.v)
                s is E.Neg -> s.e
                else -> E.Neg(s)
            }
        }
        is E.Add -> {
            val l = simplify(e.l)
            val r = simplify(e.r)
            when {
                l is E.Num && r is E.Num -> E.Num(l.v + r.v)
                l is E.Num && abs(l.v) < 1e-12 -> r
                r is E.Num && abs(r.v) < 1e-12 -> l
                else -> E.Add(l, r)
            }
        }
        is E.Sub -> {
            val l = simplify(e.l)
            val r = simplify(e.r)
            when {
                l is E.Num && r is E.Num -> E.Num(l.v - r.v)
                r is E.Num && abs(r.v) < 1e-12 -> l
                else -> E.Sub(l, r)
            }
        }
        is E.Mul -> {
            val l = simplify(e.l)
            val r = simplify(e.r)
            when {
                (l is E.Num && abs(l.v) < 1e-12) ||
                        (r is E.Num && abs(r.v) < 1e-12) -> E.Num(0.0)
                l is E.Num && abs(l.v - 1.0) < 1e-12 -> r
                r is E.Num && abs(r.v - 1.0) < 1e-12 -> l
                l is E.Num && r is E.Num -> E.Num(l.v * r.v)
                else -> E.Mul(l, r)
            }
        }
        is E.Div -> {
            val l = simplify(e.l)
            val r = simplify(e.r)
            when {
                l is E.Num && abs(l.v) < 1e-12 -> E.Num(0.0)
                r is E.Num && abs(r.v - 1.0) < 1e-12 -> l
                l is E.Num && r is E.Num -> E.Num(l.v / r.v)
                else -> E.Div(l, r)
            }
        }
        is E.Pow -> {
            val l = simplify(e.l)
            val r = simplify(e.r)
            when {
                r is E.Num && abs(r.v) < 1e-12 -> E.Num(1.0)
                r is E.Num && abs(r.v - 1.0) < 1e-12 -> l
                l is E.Num && r is E.Num -> E.Num(Math.pow(l.v, r.v))
                else -> E.Pow(l, r)
            }
        }
        is E.Fn -> E.Fn(e.name, simplify(e.arg))
    }

    // ============ 打印 ============
    private fun prec(e: E): Int = when (e) {
        is E.Num, is E.Var, is E.Fn -> 100
        is E.Neg -> 40
        is E.Pow -> 30
        is E.Mul, is E.Div -> 20
        is E.Add, is E.Sub -> 10
    }

    private fun print(e: E, parentPrec: Int = 0): String {
        val p = prec(e)
        val s = when (e) {
            is E.Num -> num(e.v)
            is E.Var -> "x"
            is E.Neg -> "-${print(e.e, 40)}"
            is E.Add -> "${print(e.l, 10)} + ${print(e.r, 10)}"
            is E.Sub -> "${print(e.l, 10)} \u2212 ${print(e.r, 11)}"
            is E.Mul -> {
                val ls = print(e.l, 20)
                val rs = print(e.r, 20)
                "$ls\u00B7$rs"
            }
            is E.Div -> "${print(e.l, 21)}/${print(e.r, 21)}"
            is E.Pow -> "${print(e.l, 30)}^${print(e.r, 29)}"
            is E.Fn -> "${e.name}(${print(e.arg, 0)})"
        }
        return if (p < parentPrec) "($s)" else s
    }

    private fun format(s: String): String =
        s.replace("*", "\u00D7").replace("/", "\u00F7").replace("-", "\u2212")
}