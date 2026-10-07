package com.tengwear.jisuanqi.logic

import java.math.BigInteger

data class NumberBaseResult(
    val decimal: String,
    val binary: String,
    val octal: String,
    val hex: String,
    val error: String?
)

object NumberBaseSolver {

    /**
     * 把 input 从 fromBase 进制转为其他三种进制
     */
    fun solve(input: String, fromBase: Int): NumberBaseResult {
        val s = input.trim().uppercase()
        if (s.isEmpty()) {
            return NumberBaseResult("", "", "", "", "输入为空")
        }

        val validDigits = when (fromBase) {
            2 -> "01"
            8 -> "01234567"
            10 -> "0123456789"
            16 -> "0123456789ABCDEF"
            else -> return NumberBaseResult("", "", "", "", "不支持的进制")
        }

        if (s.any { it !in validDigits }) {
            return NumberBaseResult(
                "", "", "", "",
                "含有非 $fromBase 进制字符：${s.filter { it !in validDigits }}"
            )
        }

        return try {
            val value = BigInteger(s, fromBase)
            NumberBaseResult(
                decimal = value.toString(10),
                binary = value.toString(2),
                octal = value.toString(8),
                hex = value.toString(16).uppercase(),
                error = null
            )
        } catch (e: Exception) {
            NumberBaseResult("", "", "", "", "转换失败：${e.message}")
        }
    }

    fun baseLabel(base: Int): String = when (base) {
        2 -> "二进制"
        8 -> "八进制"
        10 -> "十进制"
        16 -> "十六进制"
        else -> "未知进制"
    }
}