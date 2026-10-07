package com.tengwear.jisuanqi.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atanh
import kotlin.math.cbrt
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh
import kotlin.random.Random

/**
 * 科学计算状态
 */
data class ScientificState(
    val display: String = "0",
    val expression: String = "",
    val operator: String? = null,
    val operand: Double? = null,
    val isNewInput: Boolean = true,
    val hasError: Boolean = false
)

class ScientificCalcViewModel : ViewModel() {

    var state by mutableStateOf(ScientificState())
        private set

    var angleMode by mutableStateOf(AngleMode.DEG)
        private set

    var memory by mutableDoubleStateOf(0.0)
        private set

    val hasMemory: Boolean
        get() = memory != 0.0

    private var lastAnswer: Double = 0.0

    // ============ 显示表达式 ============

    val visibleExpression: String
        get() {
            val op = state.operator
            val operand = state.operand
            if (op == null || operand == null) return state.expression
            return if (state.isNewInput) {
                "${formatResult(operand)} ${opSymbol(op)}"
            } else {
                "${formatResult(operand)} ${opSymbol(op)} ${state.display}"
            }
        }

    // ============ 输入 ============

    fun onInput(value: String) {
        if (state.hasError) return
        val current = state.display
        when {
            value == "." && current.contains(".") -> return
            state.isNewInput -> {
                state = state.copy(
                    display = if (value == ".") "0." else value,
                    isNewInput = false
                )
            }
            else -> {
                if (current.length >= 14) return
                val newDisplay = if (current == "0" && value != ".") value else current + value
                state = state.copy(display = newDisplay)
            }
        }
    }

    fun onOperator(op: String) {
        if (state.hasError) return
        val currentValue = state.display.toDoubleOrNull() ?: 0.0
        val newOperand = if (state.operand != null && state.operator != null && !state.isNewInput) {
            val result = calculate(state.operand!!, currentValue, state.operator!!)
            if (result.isNaN() || result.isInfinite()) {
                state = state.copy(display = "Error", hasError = true)
                return
            }
            state = state.copy(display = formatResult(result))
            result
        } else {
            currentValue
        }
        state = state.copy(
            operand = newOperand,
            operator = op,
            isNewInput = true,
            expression = "${formatResult(newOperand)} ${opSymbol(op)}"
        )
    }

    fun onEquals() {
        if (state.hasError) return
        val op = state.operator ?: return
        val operand = state.operand ?: return
        val currentValue = state.display.toDoubleOrNull() ?: 0.0
        val result = calculate(operand, currentValue, op)
        if (result.isNaN() || result.isInfinite()) {
            state = state.copy(display = "Error", hasError = true)
        } else {
            lastAnswer = result
            val exprDisplay =
                "${formatResult(operand)} ${opSymbol(op)} ${formatResult(currentValue)} ="
            val resultStr = formatResult(result)
            state = state.copy(
                display = resultStr,
                expression = exprDisplay,
                operator = null,
                operand = null,
                isNewInput = true,
                hasError = false
            )
        }
    }

    // ============ 一元函数 ============

    fun onScientificUnary(key: String) {
        if (state.hasError) return
        val v = state.display.toDoubleOrNull() ?: return
        val r: Double = try {
            unary(key, v)
        } catch (_: Exception) {
            Double.NaN
        }
        if (r.isNaN() || r.isInfinite()) {
            state = state.copy(display = "Error", hasError = true)
        } else {
            val resultStr = formatResult(r)
            val expr = "$key(${formatResult(v)})"
            state = state.copy(
                display = resultStr,
                expression = expr,
                isNewInput = true
            )
        }
    }

    private fun unary(key: String, v: Double): Double {
        val rad = angleMode.toRadians(v)
        return when (key) {
            "sin" -> sin(rad)
            "cos" -> cos(rad)
            "tan" -> tan(rad)
            "asin" -> angleMode.fromRadians(asin(v))
            "acos" -> angleMode.fromRadians(acos(v))
            "atan" -> angleMode.fromRadians(atan(v))
            "sinh" -> sinh(v)
            "cosh" -> cosh(v)
            "tanh" -> tanh(v)
            "asinh" -> asinh(v)
            "acosh" -> acosh(v)
            "atanh" -> atanh(v)
            "ln" -> ln(v)
            "log" -> log10(v)
            "log2" -> log2(v)
            "sqrt" -> sqrt(v)
            "cbrt" -> cbrt(v)
            "sq" -> v * v
            "cube" -> v * v * v
            "10pow" -> 10.0.pow(v)
            "2pow" -> 2.0.pow(v)
            "epow" -> E.pow(v)
            "abs" -> abs(v)
            "recip" -> if (v == 0.0) Double.NaN else 1.0 / v
            "fact" -> factorial(v)
            "floor" -> floor(v)
            "ceil" -> ceil(v)
            "%" -> v / 100.0
            else -> v
        }
    }

    private fun factorial(x: Double): Double {
        if (x < 0.0) return Double.NaN
        if (x != floor(x)) return Double.NaN
        if (x > 170.0) return Double.POSITIVE_INFINITY
        var r = 1.0
        var i = 2
        val n = x.toInt()
        while (i <= n) {
            r *= i
            i++
        }
        return r
    }

    // ============ 常量 ============

    fun onConstantE() {
        if (state.hasError) return
        state = state.copy(display = formatResult(E), isNewInput = false)
    }

    fun onConstantPi() {
        if (state.hasError) return
        state = state.copy(display = formatResult(PI), isNewInput = false)
    }

    fun onConstantKey(key: String) {
        if (state.hasError) return
        val value = CONSTANTS[key] ?: return
        state = state.copy(display = formatResult(value), isNewInput = false)
    }

    // ============ EXP / DMS / 杂项 ============

    fun onExp() {
        if (state.hasError) return
        val cur = state.display
        if (cur.contains("E")) return
        if (cur.isEmpty() || cur == "-") return
        state = state.copy(display = cur + "E", isNewInput = false)
    }

    fun onDms() {
        if (state.hasError) return
        val v = state.display.toDoubleOrNull() ?: return
        val sign = if (v < 0) "-" else ""
        var av = abs(v)
        val d = av.toInt()
        av = (av - d) * 60
        val m = av.toInt()
        val s = (av - m) * 60
        state = state.copy(
            expression = String.format(
                Locale.US,
                "%s%d\u00B0%d\u2032%.2f\u2033",
                sign, d, m, s
            )
        )
    }

    fun onRandom() {
        if (state.hasError) return
        state = state.copy(display = formatResult(Random.nextDouble()), isNewInput = false)
    }

    fun onAnswer() {
        if (state.hasError) return
        state = state.copy(display = formatResult(lastAnswer), isNewInput = false)
    }

    // ============ 内存 ============

    fun memoryAdd() {
        if (state.hasError) return
        val v = state.display.toDoubleOrNull() ?: return
        memory += v
        state = state.copy(isNewInput = true)
    }

    fun memorySubtract() {
        if (state.hasError) return
        val v = state.display.toDoubleOrNull() ?: return
        memory -= v
        state = state.copy(isNewInput = true)
    }

    fun memoryRecall() {
        if (state.hasError) return
        state = state.copy(display = formatResult(memory), isNewInput = false)
    }

    fun memoryClear() {
        memory = 0.0
    }

    // ============ 模式切换 ============

    fun toggleAngleMode() {
        angleMode = angleMode.toggle()
    }

    // ============ 归零 / 退格 ============

    fun onClear() {
        state = ScientificState()
    }

    fun onBackspace() {
        if (state.hasError) {
            onClear()
            return
        }
        val current = state.display
        if (current.length <= 1) {
            state = state.copy(display = "0", isNewInput = true)
        } else {
            state = state.copy(display = current.dropLast(1))
        }
    }

    fun onToggleSign() {
        if (state.hasError) return
        val value = state.display.toDoubleOrNull() ?: return
        state = state.copy(display = formatResult(-value))
    }

    // ============ 内部工具 ============

    private fun calculate(a: Double, b: Double, op: String): Double {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b == 0.0) Double.NaN else a / b
            "pow" -> a.pow(b)
            else -> b
        }
    }

    private fun opSymbol(op: String): String = when (op) {
        "+" -> "+"
        "-" -> "-"
        "*" -> "*"
        "/" -> "/"
        "pow" -> "^"
        else -> op
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        if (value == 0.0) return "0"
        val a = abs(value)
        if (a >= 1e12 || a < 1e-9) {
            return String.format(Locale.US, "%.6E", value)
        }
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            BigDecimal(value)
                .setScale(10, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString()
        }
    }

    companion object {
        private val CONSTANTS = mapOf(
            "const_c"     to 299792458.0,
            "const_h"     to 6.62607015e-34,
            "const_k"     to 1.380649e-23,
            "const_Na"    to 6.02214076e23,
            "const_R"     to 8.314462618,
            "const_G"     to 6.67430e-11,
            "const_g"     to 9.80665,
            "const_eps0"  to 8.8541878128e-12,
            "const_mu0"   to 1.25663706212e-6,
            "const_me"    to 9.1093837015e-31,
            "const_mp"    to 1.67262192369e-27,
            "const_Me"    to 5.9722e24,
            "const_phi"   to 1.618033988749895,
            "const_gam"   to 0.5772156649015329,
            "const_tau"   to 2.0 * PI,
            "const_ln2"   to 0.6931471805599453,
            "const_ln10"  to 2.302585092994046,
            "const_log2e" to 1.4426950408889634
        )
    }
}