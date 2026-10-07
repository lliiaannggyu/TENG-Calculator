package com.tengwear.jisuanqi.logic

data class NumberChineseResult(
    val lower: String,   // 中文小写
    val upper: String,   // 财务大写
    val error: String?
)

object NumberChineseSolver {

    private const val LOWER_DIGITS = "零一二三四五六七八九"
    private const val UPPER_DIGITS = "零壹贰叁肆伍陆柒捌玖"
    private val LOWER_UNITS = arrayOf("", "十", "百", "千")
    private val UPPER_UNITS = arrayOf("", "拾", "佰", "仟")
    private val LOWER_SECTIONS = arrayOf("", "万", "亿", "万亿")
    private val UPPER_SECTIONS = arrayOf("", "万", "亿", "万亿")

    fun solve(input: String): NumberChineseResult {
        val s = input.trim()
        if (s.isEmpty()) return NumberChineseResult("", "", "输入为空")

        val clean = s.replace(" ", "")
        if (!clean.matches(Regex("^-?\\d+(\\.\\d+)?$"))) {
            return NumberChineseResult("", "", "请输入有效的数字")
        }

        val negative = clean.startsWith("-")
        val abs = if (negative) clean.substring(1) else clean

        val parts = abs.split(".")
        val intPart = parts[0]
        val fracPart = if (parts.size > 1) parts[1] else ""

        if (intPart.length > 15) {
            return NumberChineseResult("", "", "整数部分过长（最大 15 位）")
        }
        val intVal = intPart.toLongOrNull()
            ?: return NumberChineseResult("", "", "整数部分无法解析")

        // ---- 中文小写 ----
        val lowerInt = intToChinese(intVal, LOWER_DIGITS, LOWER_UNITS, LOWER_SECTIONS)
        val lowerFrac = if (fracPart.isEmpty()) ""
        else "点" + fracPart.map { LOWER_DIGITS[it - '0'] }.joinToString("")
        val lowerPrefix = if (negative) "负" else ""
        val lower = lowerPrefix + lowerInt + lowerFrac

        // ---- 财务大写 ----
        val upperInt = intToChinese(intVal, UPPER_DIGITS, UPPER_UNITS, UPPER_SECTIONS)
        val upperPrefix = if (negative) "负" else ""
        val upper = upperPrefix + upperFinancial(intVal, fracPart, upperInt)

        return NumberChineseResult(lower, upper, null)
    }

    private fun intToChinese(
        num: Long,
        digits: String,
        units: Array<String>,
        sections: Array<String>
    ): String {
        if (num == 0L) return digits[0].toString()

        var n = num
        val secArr = mutableListOf<Int>()
        while (n > 0) {
            secArr.add((n % 10000).toInt())
            n /= 10000
        }

        val sb = StringBuilder()
        var needZero = false

        for (i in secArr.indices.reversed()) {
            val sec = secArr[i]
            if (sec == 0) {
                if (sb.isNotEmpty()) needZero = true
                continue
            }
            if (sb.isNotEmpty()) {
                if (needZero || sec < 1000) {
                    sb.append(digits[0])
                }
            }
            sb.append(fourDigitsToChinese(sec, digits, units))
            sb.append(sections[i])
            needZero = false
        }

        var result = sb.toString()
        // 口语化："一十" → "十"（仅整体最高位）
        if (result.startsWith("一十") || result.startsWith("壹拾")) {
            result = result.substring(1)
        }
        return result
    }

    private fun fourDigitsToChinese(
        sec: Int,
        digits: String,
        units: Array<String>
    ): String {
        if (sec == 0) return ""
        val s = sec.toString().padStart(4, '0')
        var lastNonZero = -1
        for (i in 0 until 4) {
            if (s[i] != '0') lastNonZero = i
        }
        if (lastNonZero < 0) return ""

        val sb = StringBuilder()
        var zeroFlag = false
        for (i in 0..lastNonZero) {
            val d = s[i] - '0'
            if (d == 0) {
                zeroFlag = true
            } else {
                if (zeroFlag) {
                    sb.append(digits[0])
                    zeroFlag = false
                }
                sb.append(digits[d]).append(units[3 - i])
            }
        }
        return sb.toString()
    }

    /**
     * 大写财务形式：
     * - 无小数：X元整
     * - 有角无分：X元Y角
     * - 无角有分：X元零Y分
     * - 有角有分：X元Y角Z分
     */
    private fun upperFinancial(intVal: Long, frac: String, upperInt: String): String {
        if (frac.isEmpty()) {
            return upperInt + "元整"
        }

        val f = frac.take(2).padEnd(2, '0')
        val jiao = f[0] - '0'
        val fen = f[1] - '0'

        if (jiao == 0 && fen == 0) {
            return upperInt + "元整"
        }

        val sb = StringBuilder()
        sb.append(upperInt).append("元")

        when {
            jiao == 0 -> {
                sb.append(UPPER_DIGITS[0])
                sb.append(UPPER_DIGITS[fen]).append("分")
            }
            fen == 0 -> {
                sb.append(UPPER_DIGITS[jiao]).append("角")
            }
            else -> {
                sb.append(UPPER_DIGITS[jiao]).append("角")
                sb.append(UPPER_DIGITS[fen]).append("分")
            }
        }

        return sb.toString()
    }
}