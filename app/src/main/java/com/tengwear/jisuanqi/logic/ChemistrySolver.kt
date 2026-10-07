package com.tengwear.jisuanqi.logic

import kotlin.math.abs
import kotlin.math.roundToInt

data class ChemistryResult(
    val balanced: String,       // 配平后的完整方程式
    val coefficients: List<Int>,// 系数（先反应物后生成物）
    val steps: List<String>,    // 中间步骤
    val error: String?
)

/**
 * 化学方程式配平器
 *
 * 支持：
 * - 反应物和生成物均为中性分子
 * - 分子式含括号、数字下标，如 Ca(OH)2、Fe2(SO4)3
 * - 输入中箭头可用 "=" 或 "->" 或 "→"
 *
 * 原理：
 *   对每种元素，反应物中各分子系数×原子数之和 = 生成物中各分子系数×原子数之和
 *   组成齐次线性方程组 A·x = 0，求整数零空间向量（取最简）
 */
object ChemistrySolver {

    fun balance(input: String): ChemistryResult {
        val raw = input.trim()
        if (raw.isEmpty()) {
            return ChemistryResult("", emptyList(), emptyList(), "输入为空")
        }

        // 归一化箭头
        val normalized = raw
            .replace("\u2192", "=")   // →
            .replace("->", "=")
            .replace("=>", "=")
            .replace("\uFF1D", "=")   // ＝
            .replace(" ", "")

        val eqIdx = normalized.indexOf('=')
        if (eqIdx <= 0 || eqIdx >= normalized.length - 1) {
            return ChemistryResult("", emptyList(), emptyList(), "请使用 = 或 → 分隔反应物和生成物")
        }

        val left = normalized.substring(0, eqIdx)
        val right = normalized.substring(eqIdx + 1)

        val reactants = left.split("+").map { it.trim() }.filter { it.isNotEmpty() }
        val products  = right.split("+").map { it.trim() }.filter { it.isNotEmpty() }

        if (reactants.isEmpty() || products.isEmpty()) {
            return ChemistryResult("", emptyList(), emptyList(), "反应物或生成物为空")
        }

        // 解析所有分子式
        val leftMaps = ArrayList<Map<String, Int>>(reactants.size)
        for (f in reactants) {
            val m = parseFormula(f)
                ?: return ChemistryResult("", emptyList(), emptyList(), "无法解析：$f")
            leftMaps.add(m)
        }
        val rightMaps = ArrayList<Map<String, Int>>(products.size)
        for (f in products) {
            val m = parseFormula(f)
                ?: return ChemistryResult("", emptyList(), emptyList(), "无法解析：$f")
            rightMaps.add(m)
        }

        // 校验所有元素符号是否真实存在
        val allSymbols = (leftMaps.flatMap { it.keys } + rightMaps.flatMap { it.keys })
            .toSortedSet()
        for (sym in allSymbols) {
            if (PeriodicElements.find(sym) == null) {
                return ChemistryResult("", emptyList(), emptyList(), "未知元素符号：$sym")
            }
        }

        val m = reactants.size
        val n = products.size
        val total = m + n
        val k = allSymbols.size

        // 建立系数矩阵 A（k × total）
        val A = Array(k) { DoubleArray(total) }
        val symbolList = allSymbols.toList()
        for (ei in symbolList.indices) {
            val sym = symbolList[ei]
            for (j in 0 until m) {
                A[ei][j] = (leftMaps[j][sym] ?: 0).toDouble()
            }
            for (j in 0 until n) {
                A[ei][m + j] = -(rightMaps[j][sym] ?: 0).toDouble()
            }
        }

        // 求零空间
        val basis = nullSpace(A, total)
            ?: return ChemistryResult("", emptyList(), emptyList(), "无法求解（矩阵无解）")

        if (basis.isEmpty()) {
            return ChemistryResult("", emptyList(), emptyList(), "只有零解（方程式无法配平）")
        }

        // 选取最简的一组整数解
        var bestCoeffs: List<Int>? = null
        var bestScore = Int.MAX_VALUE
        for (v in basis) {
            val c = toIntCoeffs(v) ?: continue
            val score = c.sum()
            if (score in 1 until bestScore) {
                bestScore = score
                bestCoeffs = c
            }
        }
        val coeffs = bestCoeffs
            ?: return ChemistryResult("", emptyList(), emptyList(), "无法得到整数系数")

        // 生成结果字符串
        val sb = StringBuilder()
        for (i in 0 until m) {
            if (i > 0) sb.append(" + ")
            if (coeffs[i] != 1) sb.append(coeffs[i])
            sb.append(reactants[i])
        }
        sb.append(" = ")
        for (i in 0 until n) {
            if (i > 0) sb.append(" + ")
            val c = coeffs[m + i]
            if (c != 1) sb.append(c)
            sb.append(products[i])
        }

        // 生成步骤
        val steps = ArrayList<String>()
        steps.add("元素种类：${symbolList.joinToString(" ")}")
        steps.add("反应物 $m 项，生成物 $n 项，共 $total 个未知系数")
        steps.add("按原子守恒列方程，解线性方程组，取最简整数解")
        steps.add("")
        for (i in 0 until total) {
            val label = if (i < m) reactants[i] else products[i - m]
            steps.add("  ${label} 系数 = ${coeffs[i]}")
        }

        return ChemistryResult(sb.toString(), coeffs, steps, null)
    }

    /**
     * 解析分子式 → 元素符号:原子数
     * 支持括号：Ca(OH)2、Fe2(SO4)3
     * 返回 null 表示格式非法
     */
    fun parseFormula(formula: String): Map<String, Int>? {
        val stack = ArrayDeque<MutableMap<String, Int>>()
        stack.addLast(mutableMapOf())
        var i = 0
        while (i < formula.length) {
            val c = formula[i]
            when {
                c == '(' || c == '\uFF08' -> {
                    stack.addLast(mutableMapOf())
                    i++
                }
                c == ')' || c == '\uFF09' -> {
                    if (stack.size < 2) return null
                    val top = stack.removeLast()
                    val (cnt, next) = readNumber(formula, i + 1)
                    val mult = if (cnt > 0) cnt else 1
                    i = next
                    val parent = stack.last()
                    for ((k, v) in top) {
                        parent[k] = (parent[k] ?: 0) + v * mult
                    }
                }
                c.isUpperCase() -> {
                    // 元素符号：大写字母 + 最多一个小写字母
                    var j = i + 1
                    if (j < formula.length && formula[j].isLowerCase()) j++
                    val sym = formula.substring(i, j)
                    val (cnt, next) = readNumber(formula, j)
                    val count = if (cnt > 0) cnt else 1
                    i = next
                    val top = stack.last()
                    top[sym] = (top[sym] ?: 0) + count
                }
                c.isDigit() -> return null  // 数字必须跟在元素或右括号后
                c == '.' || c == '\u00B7' -> return null // 暂不支持水合物
                else -> return null
            }
        }
        if (stack.size != 1) return null
        val result = stack.last()
        if (result.isEmpty()) return null
        return result
    }

    private fun readNumber(s: String, start: Int): Pair<Int, Int> {
        var j = start
        while (j < s.length && s[j].isDigit()) j++
        if (j == start) return 0 to start
        val v = s.substring(start, j).toIntOrNull() ?: return 0 to start
        return v to j
    }

    /**
     * 求齐次线性方程组 A·x = 0 的零空间基
     * A 是 k × n 矩阵
     */
    private fun nullSpace(A: Array<DoubleArray>, n: Int): List<DoubleArray>? {
        val rows = A.size
        if (rows == 0) return null
        val M = Array(rows) { A[it].copyOf() }

        val pivotCols = ArrayList<Int>()
        var row = 0
        for (col in 0 until n) {
            var pivot = -1
            var maxV = 1e-9
            for (r in row until rows) {
                if (abs(M[r][col]) > maxV) {
                    maxV = abs(M[r][col])
                    pivot = r
                }
            }
            if (pivot < 0) continue

            // 交换
            val tmp = M[row]; M[row] = M[pivot]; M[pivot] = tmp

            // 归一化
            val pv = M[row][col]
            for (c in 0 until n) M[row][c] /= pv

            // 消元
            for (r in 0 until rows) {
                if (r != row && abs(M[r][col]) > 1e-12) {
                    val f = M[r][col]
                    for (c in 0 until n) M[r][c] -= f * M[row][c]
                }
            }

            pivotCols.add(col)
            row++
            if (row >= rows) break
        }

        val freeCols = (0 until n).filter { it !in pivotCols }
        if (freeCols.isEmpty()) return emptyList()

        val solutions = ArrayList<DoubleArray>()
        for (fc in freeCols) {
            val v = DoubleArray(n)
            v[fc] = 1.0
            for ((i, pc) in pivotCols.withIndex()) {
                v[pc] = -M[i][fc]
            }
            solutions.add(v)
        }
        return solutions
    }

    /**
     * 把实数解向量转为最简正整数系数
     */
    private fun toIntCoeffs(vec: DoubleArray): List<Int>? {
        // 最小非零绝对值
        var minAbs = Double.MAX_VALUE
        for (v in vec) {
            val a = abs(v)
            if (a > 1e-9 && a < minAbs) minAbs = a
        }
        if (minAbs == Double.MAX_VALUE) return null

        val scaled = DoubleArray(vec.size) { vec[it] / minAbs }

        // 尝试不同倍数使其接近整数
        for (mult in 1..500) {
            var ok = true
            val ints = IntArray(scaled.size)
            for (i in scaled.indices) {
                val r = (scaled[i] * mult).roundToInt()
                if (abs(scaled[i] * mult - r) > 1e-3) {
                    ok = false
                    break
                }
                ints[i] = r
            }
            if (!ok) continue

            val nonZero = ints.filter { it != 0 }
            if (nonZero.isEmpty()) continue
            val allPos = nonZero.all { it > 0 }
            val allNeg = nonZero.all { it < 0 }
            if (!(allPos || allNeg)) continue

            val absInts = ints.map { abs(it) }
            val g = absInts.reduce { a, b -> gcd(a, b) }
            if (g <= 0) continue
            return absInts.map { it / g }
        }

        // 兜底
        val ints = scaled.map { abs(it.roundToInt()).coerceAtLeast(1) }
        val g = ints.reduce { a, b -> gcd(a, b) }
        return ints.map { it / g }
    }

    private fun gcd(a: Int, b: Int): Int {
        var x = a
        var y = b
        while (y != 0) {
            val t = y
            y = x % y
            x = t
        }
        return x
    }
}