package com.tengwear.jisuanqi.logic

import java.math.BigDecimal
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

data class ScientificResult(
    val plain: String,       // 普通十进制形式
    val scientific: String,  // 科学计数法形式
    val value: Double,
    val error: String?
)

object ScientificNotationSolver {

    /**
     * 输入可以是：
     * - 普通数字：12345、0.0015、-3.14
     * - 科学计数法：1.2345E4、1.5E-3、1.2345e4
     * - Unicode 形式：1.2345×10^4、1.5×10^−3
     */
    fun solve(input: String): ScientificResult {
        val s = input.trim()
        if (s.isEmpty()) {
            return ScientificResult("", "", 0.0, "输入为空")
        }

        // 归一化：所有科学计数法写法统一成 "数字E指数"
        val normalized = s
            .replace(" ", "")
            .replace("\u00D710^", "E")   // ×10^ → E
            .replace("\u00D7", "")       // 去掉残留的 ×
            .replace("^", "")            // 去掉残留的 ^
            .replace("\u2212", "-")      // Unicode 减号 → ASCII
            .replace("e", "E")

        val v = normalized.toDoubleOrNull()
            ?: return ScientificResult("", "", 0.0, "无法解析：$s")

        if (v.isNaN() || v.isInfinite()) {
            return ScientificResult("", "", 0.0, "数值无效")
        }

        val plain = toPlain(v)
        val sci = toScientific(v)

        return ScientificResult(plain, sci, v, null)
    }

    /**
     * 普通十进制形式 — 用 BigDecimal 避免 Java 的 E 记法
     */
    private fun toPlain(v: Double): String {
        return try {
            val bd = BigDecimal(v.toString()).stripTrailingZeros()
            bd.toPlainString()
        } catch (e: Exception) {
            v.toString()
        }
    }

    /**
     * 科学计数法形式：a × 10^n，其中 1 ≤ |a| < 10
     */
    private fun toScientific(v: Double): String {
        if (v == 0.0) return "0"

        val absV = abs(v)
        val e0 = floor(log10(absV)).toInt()
        var m = v / 10.0.pow(e0)
        var e = e0

        // 处理浮点误差（如 9.9999... → 10）
        if (abs(m) >= 10.0) {
            m /= 10.0
            e += 1
        } else if (abs(m) < 1.0) {
            m *= 10.0
            e -= 1
        }

        val mantissa = formatMantissa(m)
        val expStr = if (e >= 0) e.toString() else "\u2212${abs(e)}"
        return "$mantissa \u00D7 10^$expStr"
    }

    private fun formatMantissa(m: Double): String {
        val s = String.format("%.10f", m)
        val trimmed = s.trimEnd('0').trimEnd('.')
        return if (trimmed.isEmpty() || trimmed == "-") "0" else trimmed
    }
}