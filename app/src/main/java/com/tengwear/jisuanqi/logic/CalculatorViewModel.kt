package com.tengwear.jisuanqi.logic

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs

data class CalculatorState(
    /** 用户正在编辑的整个算式字符串（如 "12+34"） */
    val display: String = "0",
    val isNewInput: Boolean = true,
    val hasError: Boolean = false,
    val displayApprox: Boolean = false,
    val isCalculating: Boolean = false,
    val calcProgress: Float = 0f,
    val cursorPos: Int = 0
)

data class HistoryEntry(val expression: String, val result: String)
data class HistoryGroup(val id: Long, val entries: List<HistoryEntry>)

class CalculatorViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var state by mutableStateOf(CalculatorState())
        private set

    var angleMode by mutableStateOf(AngleMode.DEG)
        private set

    var memory by mutableStateOf(Rational.ZERO)
        private set

    val hasMemory: Boolean
        get() = !memory.isZero()

    val historyGroups = mutableStateListOf<HistoryGroup>()
    val currentSession = mutableStateListOf<HistoryEntry>()
    private var lastAnswer: Rational = Rational.ZERO

    private val MAX_DISPLAY_DIGITS = 1000
    private var currentJob: Job? = null

    init {
        loadHistory()
    }

    // ============ 输入 ============

    fun onInput(value: String) {
        if (state.hasError || state.isCalculating) return
        val cur = state.display
        val pos = state.cursorPos.coerceIn(0, cur.length)

        // 结果刚出来时的第一次输入 → 完全替换
        if (state.isNewInput) {
            val nd = if (value == ".") "0." else value
            state = state.copy(
                display = nd,
                cursorPos = nd.length,
                isNewInput = false,
                displayApprox = false
            )
            return
        }

        // 整个字符串里只允许一个小数点
        if (value == "." && cur.contains(".")) return

        // 光标在末尾且当前是纯 "0" → 替换
        if (cur == "0" && value != ".") {
            state = state.copy(
                display = value,
                cursorPos = value.length,
                displayApprox = false
            )
            return
        }

        val nd = cur.substring(0, pos) + value + cur.substring(pos)
        state = state.copy(
            display = nd,
            cursorPos = pos + value.length,
            displayApprox = false
        )
    }

    /** 运算符按钮：直接把符号插入到光标处 */
    fun onOperator(op: String) {
        if (state.hasError || state.isCalculating) return

        val cur = state.display
        val pos = state.cursorPos.coerceIn(0, cur.length)

        val base = cur
        val adjustedPos = pos.coerceIn(0, base.length)

        // 首字符是运算符时，把 - 当负号允许，其他忽略
        val isFirst = base.isEmpty() || (base.length == 1 && base == "0")
        if (isFirst && op != "-") {
            if (op == "+" || op == "*" || op == "/" || op == "^" || op == "√") return
        }

        val insertText = when (op) {
            "root" -> "√"
            "pow" -> "^"
            else -> op
        }

        val nd = base.substring(0, adjustedPos) + insertText + base.substring(adjustedPos)
        state = state.copy(
            display = nd,
            cursorPos = adjustedPos + insertText.length,
            isNewInput = false,
            displayApprox = false
        )
    }

    // ============ 求值 ============

    fun onEquals() {
        if (state.hasError || state.isCalculating) return
        val expr = state.display.trim()
        if (expr.isEmpty()) return

        state = state.copy(isCalculating = true, calcProgress = 0.05f, displayApprox = false)

        currentJob = viewModelScope.launch {
            val result: ExpressionEval.Result? = try {
                withContext(Dispatchers.Default) {
                    ExpressionEval.eval(expr) { p ->
                        state = state.copy(calcProgress = p)
                    }
                }
            } catch (_: Exception) {
                null
            }

            if (result == null) {
                state = state.copy(
                    display = "Error",
                    hasError = true,
                    isCalculating = false,
                    calcProgress = 0f,
                    cursorPos = 0
                )
                currentJob = null
                return@launch
            }

            lastAnswer = result.value
            val resultStr = formatRational(result.value)
            val exprDisplay = "$expr ="

            state = state.copy(
                display = resultStr,
                isNewInput = true,
                hasError = false,
                displayApprox = result.isApprox,
                isCalculating = false,
                calcProgress = 0f,
                cursorPos = resultStr.length
            )
            currentSession.add(HistoryEntry(exprDisplay, resultStr))
            currentJob = null
        }
    }

    private fun cancelCurrentJob() {
        currentJob?.cancel()
        currentJob = null
        if (state.isCalculating) {
            state = state.copy(isCalculating = false, calcProgress = 0f)
        }
    }

    // ============ 光标 ============

    fun onCursorLeft() {
        if (state.hasError || state.isCalculating) return
        val newPos = (state.cursorPos - 1).coerceAtLeast(0)
        state = state.copy(cursorPos = newPos)
    }

    fun onCursorRight() {
        if (state.hasError || state.isCalculating) return
        val newPos = (state.cursorPos + 1).coerceAtMost(state.display.length)
        state = state.copy(cursorPos = newPos)
    }

    fun setCursorPosition(pos: Int) {
        if (state.hasError || state.isCalculating) return
        val newPos = pos.coerceIn(0, state.display.length)
        state = state.copy(cursorPos = newPos, isNewInput = false)
    }

    // ============ 一元函数（暂保留接口） ============

    fun onScientificUnary(key: String) {
        if (state.hasError || state.isCalculating) return
        val s = state.display.trim()
        val r = Rational.parse(s) ?: return
        val expr = when (key) {
            "sqrt" -> "√(${formatRational(r, true)})"
            "sq" -> "(${formatRational(r, true)})^2"
            "cube" -> "(${formatRational(r, true)})^3"
            "abs" -> "|${formatRational(r, true)}|"
            else -> "$key(${formatRational(r, true)})"
        }
        val newDisplay = state.display + expr
        state = state.copy(display = newDisplay, cursorPos = newDisplay.length, isNewInput = false)
    }

    // ============ 常量 ============

    fun onConstantE() { insertConstant(formatDouble(Math.E)) }
    fun onConstantPi() { insertConstant(formatDouble(Math.PI)) }
    fun onConstantKey(key: String) {
        val v = CONSTANTS[key] ?: return
        insertConstant(formatDouble(v))
    }
    private fun insertConstant(s: String) {
        if (state.hasError || state.isCalculating) return
        if (state.isNewInput) {
            state = state.copy(display = s, cursorPos = s.length, isNewInput = false)
            return
        }
        val pos = state.cursorPos.coerceIn(0, state.display.length)
        val nd = state.display.substring(0, pos) + s + state.display.substring(pos)
        state = state.copy(display = nd, cursorPos = pos + s.length, displayApprox = false)
    }

    // ============ EXP / DMS / 杂项 ============

    fun onExp() {
        if (state.hasError || state.isCalculating) return
        if (state.display.contains("E")) return
        if (state.display.isEmpty() || state.display == "-") return
        val nd = state.display + "E"
        state = state.copy(display = nd, cursorPos = nd.length, isNewInput = false)
    }

    fun onDms() {
        if (state.hasError || state.isCalculating) return
        val v = state.display.toDoubleOrNull() ?: return
        val sign = if (v < 0) "-" else ""
        var av = abs(v)
        val d = av.toInt(); av = (av - d) * 60
        val m = av.toInt(); val s = (av - m) * 60
        val nd = String.format(Locale.US, "%s%d.%02d%02d", sign, d, m, (s * 100).toInt() / 100)
        state = state.copy(display = nd, cursorPos = nd.length, isNewInput = true)
    }

    fun onRandom() {
        if (state.hasError || state.isCalculating) return
        val s = formatDouble(Math.random())
        if (state.isNewInput) {
            state = state.copy(display = s, cursorPos = s.length, isNewInput = false)
        } else {
            val pos = state.cursorPos.coerceIn(0, state.display.length)
            val nd = state.display.substring(0, pos) + s + state.display.substring(pos)
            state = state.copy(display = nd, cursorPos = pos + s.length)
        }
    }

    fun onAnswer() {
        if (state.hasError || state.isCalculating) return
        val s = formatRational(lastAnswer)
        if (state.isNewInput) {
            state = state.copy(display = s, cursorPos = s.length, isNewInput = false)
        } else {
            val pos = state.cursorPos.coerceIn(0, state.display.length)
            val nd = state.display.substring(0, pos) + s + state.display.substring(pos)
            state = state.copy(display = nd, cursorPos = pos + s.length)
        }
    }

    // ============ 内存 ============

    fun memoryAdd() {
        if (state.hasError || state.isCalculating) return
        val v = Rational.parse(state.display) ?: return
        memory += v
        state = state.copy(isNewInput = true)
    }
    fun memorySubtract() {
        if (state.hasError || state.isCalculating) return
        val v = Rational.parse(state.display) ?: return
        memory -= v
        state = state.copy(isNewInput = true)
    }
    fun memoryRecall() {
        if (state.hasError || state.isCalculating) return
        val s = formatRational(memory)
        state = state.copy(display = s, cursorPos = s.length, isNewInput = true)
    }
    fun memoryClear() { memory = Rational.ZERO }

    // ============ 模式 ============

    fun toggleAngleMode() { angleMode = angleMode.toggle() }

    // ============ 归零 / 退格 ============

    fun onClear() {
        if (state.isCalculating) { cancelCurrentJob(); return }
        commitSession()
        state = CalculatorState()
    }

    fun onBackspace() {
        if (state.hasError) { onClear(); return }
        val cur = state.display
        val pos = state.cursorPos.coerceIn(0, cur.length)
        if (pos <= 0) return

        val nd = cur.substring(0, pos - 1) + cur.substring(pos)
        state = state.copy(
            display = if (nd.isEmpty()) "0" else nd,
            cursorPos = if (nd.isEmpty()) 0 else pos - 1,
            isNewInput = nd.isEmpty(),
            displayApprox = false
        )
    }

    fun onToggleSign() {
        if (state.hasError || state.isCalculating) return
        val v = Rational.parse(state.display) ?: return
        val s = formatRational(v.negate())
        state = state.copy(display = s, cursorPos = s.length)
    }

    // ============ 历史 ============

    fun insertResult(result: String) {
        if (state.hasError || state.isCalculating) return
        if (state.isNewInput) {
            state = state.copy(
                display = result, isNewInput = false,
                hasError = false, displayApprox = false,
                cursorPos = result.length
            )
        } else {
            val pos = state.cursorPos.coerceIn(0, state.display.length)
            val nd = state.display.substring(0, pos) + result + state.display.substring(pos)
            state = state.copy(display = nd, cursorPos = pos + result.length)
        }
    }

    fun insertExpression(entry: HistoryEntry) { insertResult(entry.result) }
    fun insertGroup(group: HistoryGroup) {
        val last = group.entries.lastOrNull() ?: return
        insertResult(last.result)
    }

    /**
     * ★ 点击历史条目 → 把该条的表达式（去掉末尾 " ="）放回输入框
     */
    fun editHistoryEntry(index: Int) {
        if (state.isCalculating || state.hasError) return
        val entry = currentSession.getOrNull(index) ?: return
        // 表达式存的是 "12+34 ="，去掉 " =" 后缀
        val expr = entry.expression.removeSuffix(" =").trim()
        state = state.copy(
            display = expr,
            cursorPos = expr.length,
            isNewInput = false,
            hasError = false,
            displayApprox = false
        )
    }

    /**
     * ★ 长按历史条目 → 从当前会话里删除这一条
     */
    fun deleteHistoryEntry(index: Int) {
        if (index in currentSession.indices) {
            currentSession.removeAt(index)
        }
    }

    fun clearAllHistory() {
        historyGroups.clear()
        currentSession.clear()
        persistHistory()
    }

    /**
     * ★ 对外公开：把当前会话打包成一个历史分组。
     * 入口：进历史记录页、退出应用、长按 AC、出错后清屏。
     */
    fun commitSession() {
        if (currentSession.isEmpty()) return
        historyGroups.add(
            0,
            HistoryGroup(id = System.currentTimeMillis(), entries = currentSession.toList())
        )
        currentSession.clear()
        if (historyGroups.size > MAX_GROUPS) {
            historyGroups.removeRange(MAX_GROUPS, historyGroups.size)
        }
        persistHistory()
    }

    /**
     * ★ 应用退到后台（切桌面 / 划走 / 锁屏）时调用：
     * 先把未提交的会话提交，再落盘。
     */
    fun onAppBackground() {
        commitSession()
        persistHistory()
    }

    // ============ 持久化 ============

    private fun persistHistory() {
        val sb = StringBuilder()
        historyGroups.forEach { g ->
            g.entries.forEach { e ->
                if (sb.isNotEmpty()) sb.append(SEP_RECORD)
                sb.append(g.id).append(SEP_FIELD)
                    .append(e.expression).append(SEP_FIELD)
                    .append(e.result)
            }
        }
        prefs.edit().putString(KEY_GROUPS, sb.toString()).apply()
    }

    private fun loadHistory() {
        val s = prefs.getString(KEY_GROUPS, null) ?: return
        if (s.isEmpty()) return
        val map = LinkedHashMap<Long, MutableList<HistoryEntry>>()
        s.split(SEP_RECORD).forEach { rec ->
            val parts = rec.split(SEP_FIELD)
            if (parts.size == 3) {
                val id = parts[0].toLongOrNull() ?: return@forEach
                map.getOrPut(id) { mutableListOf() }
                    .add(HistoryEntry(parts[1], parts[2]))
            }
        }
        historyGroups.clear()
        map.forEach { (id, entries) -> historyGroups.add(HistoryGroup(id, entries)) }
        if (historyGroups.size > MAX_GROUPS) {
            while (historyGroups.size > MAX_GROUPS) {
                historyGroups.removeAt(historyGroups.size - 1)
            }
        }
    }

    // ============ 格式化 ============

    private fun formatRational(r: Rational, short: Boolean = false): String {
        val den = r.den
        val num = r.num
        val isNeg = num.signum() < 0
        val absNum = num.abs()

        val intPart = absNum.divide(den)
        var rem = absNum.mod(den)

        val sb = StringBuilder()
        if (isNeg && (intPart.signum() != 0 || rem.signum() != 0)) sb.append('-')
        sb.append(intPart.toString())

        if (rem.signum() == 0) return sb.toString()

        val maxDigits = if (short) 30 else MAX_DISPLAY_DIGITS
        val digits = StringBuilder()
        val seen = HashMap<BigInteger, Int>()
        var cycleStart = -1
        var i = 0

        while (rem.signum() != 0 && i < maxDigits) {
            val p = seen[rem]
            if (p != null) { cycleStart = p; break }
            seen[rem] = i
            rem = rem.multiply(BigInteger.TEN)
            digits.append(rem.divide(den).toString())
            rem = rem.mod(den)
            i++
        }

        sb.append('.')
        if (cycleStart >= 0) {
            sb.append(digits.substring(0, cycleStart))
            sb.append('{')
            sb.append(digits.substring(cycleStart))
            sb.append('}')
        } else {
            sb.append(digits)
            if (rem.signum() != 0) sb.append("...")
        }
        return sb.toString()
    }

    private fun formatDouble(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        if (value == 0.0) return "0"
        val a = abs(value)
        if (a >= 1e15 || a < 1e-9) return String.format(Locale.US, "%.6E", value)
        if (value == value.toLong().toDouble()) return value.toLong().toString()
        return BigDecimal(value)
            .setScale(10, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
    }

    companion object {
        private const val MAX_GROUPS = 30

        // ★ 持久化用
        private const val PREFS_NAME = "calc_history"
        private const val KEY_GROUPS = "groups"
        private const val SEP_FIELD = "\u001F"   // 一条记录内部：id | expr | result
        private const val SEP_RECORD = "\u001E"  // 记录之间

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
            "const_tau"   to 2.0 * Math.PI,
            "const_ln2"   to 0.6931471805599453,
            "const_ln10"  to 2.302585092994046,
            "const_log2e" to 1.4426950408889634
        )
    }
}