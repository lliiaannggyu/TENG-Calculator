package com.tengwear.jisuanqi.logic

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * 多项式系数
 * coeffs[i] 是 x^i 的系数
 */
data class PolyCoeffs(val coeffs: DoubleArray) {
    val degree: Int get() = coeffs.size - 1
    operator fun get(i: Int): Double = coeffs.getOrElse(i) { 0.0 }
}

/**
 * 多项式识别与求解
 *
 * - fit()：采样拟合，判断表达式是否为多项式
 * - solve()：求所有实根
 * - steps()：生成解题步骤
 */
object PolynomialSolver {

    // 采样点（避开整数，减少数值误差）
    private val XS = doubleArrayOf(-2.5, -1.5, -0.5, 0.5, 1.5, 2.5, 3.5, 4.5)

    /**
     * 尝试把表达式拟合为多项式
     * 返回 null 说明不是多项式（含 sin/ln 等）
     */
    fun fit(expr: String, maxDeg: Int = 4): PolyCoeffs? {
        if (expr.isBlank()) return null
        val ys = DoubleArray(XS.size) { i ->
            ExpressionEvaluator.evaluate(expr, XS[i]) ?: return null
        }

        for (deg in 1..maxDeg) {
            val n = deg + 1
            if (n > XS.size) break
            val A = Array(n) { i -> DoubleArray(n) { j -> XS[i].pow(j) } }
            val b = DoubleArray(n) { i -> ys[i] }
            val coef = gaussian(A, b) ?: continue
            val err = maxError(coef, ys)
            if (err < 1e-5) return PolyCoeffs(coef)
        }
        return null
    }

    private fun maxError(coef: DoubleArray, ys: DoubleArray): Double {
        var m = 0.0
        for (i in ys.indices) {
            var p = 0.0
            for (j in coef.indices) p += coef[j] * XS[i].pow(j)
            m = maxOf(m, abs(p - ys[i]))
        }
        return m
    }

    private fun gaussian(A: Array<DoubleArray>, b: DoubleArray): DoubleArray? {
        val n = A.size
        val M = Array(n) { i -> DoubleArray(n + 1) { j -> if (j < n) A[i][j] else b[i] } }
        for (col in 0 until n) {
            var pivot = col
            for (row in col + 1 until n) {
                if (abs(M[row][col]) > abs(M[pivot][col])) pivot = row
            }
            if (abs(M[pivot][col]) < 1e-12) return null
            val t = M[col]; M[col] = M[pivot]; M[pivot] = t
            for (row in col + 1 until n) {
                val f = M[row][col] / M[col][col]
                for (k in col..n) M[row][k] -= f * M[col][k]
            }
        }
        val x = DoubleArray(n)
        for (i in n - 1 downTo 0) {
            var s = M[i][n]
            for (j in i + 1 until n) s -= M[i][j] * x[j]
            x[i] = s / M[i][i]
        }
        return x
    }

    fun solve(c: PolyCoeffs): List<Double> {
        val a = c.coeffs
        var deg = a.size - 1
        while (deg > 0 && abs(a[deg]) < 1e-12) deg--

        return when (deg) {
            0 -> emptyList()
            1 -> {
                val x = -a[0] / a[1]
                if (x.isNaN() || x.isInfinite()) emptyList() else listOf(x)
            }
            2 -> {
                val A = a[2]; val B = a[1]; val C = a[0]
                val d = B * B - 4 * A * C
                when {
                    d < -1e-9 -> emptyList()
                    abs(d) < 1e-9 -> listOf(-B / (2 * A))
                    else -> {
                        val sq = sqrt(d)
                        listOf((-B + sq) / (2 * A), (-B - sq) / (2 * A)).sorted()
                    }
                }
            }
            else -> solveNumeric(c)
        }
    }

    private fun solveNumeric(c: PolyCoeffs): List<Double> {
        val f = { x: Double ->
            var v = 0.0
            for (i in c.coeffs.indices) v += c.coeffs[i] * x.pow(i)
            v
        }
        return scanRoots({ x -> f(x) }, -100.0, 100.0)
    }

    fun steps(c: PolyCoeffs): List<String> {
        val a = c.coeffs
        var deg = a.size - 1
        while (deg > 0 && abs(a[deg]) < 1e-12) deg--

        val out = ArrayList<String>()
        out.add("整理为：${formatPoly(a, deg)} = 0")

        when (deg) {
            0 -> out.add("无 x 项")
            1 -> {
                val A = a[1]; val B = a[0]
                out.add("一元一次方程")
                out.add("  a = ${num(A)}，b = ${num(B)}")
                out.add("移项：${num(A)}x = ${num(-B)}")
                out.add("x = ${num(-B)} / ${num(A)}")
                out.add("解得：x = ${num(-B / A)}")
            }
            2 -> {
                val A = a[2]; val B = a[1]; val C = a[0]
                val d = B * B - 4 * A * C
                out.add("一元二次方程")
                out.add("  a = ${num(A)}，b = ${num(B)}，c = ${num(C)}")
                out.add("判别式 Δ = b\u00B2 \u2212 4ac")
                out.add("  = ${num(B)}\u00B2 \u2212 4\u00D7${num(A)}\u00D7${num(C)}")
                out.add("  = ${num(d)}")
                when {
                    d > 1e-9 -> {
                        out.add("Δ > 0，有两个不同实根")
                        out.add("x = (\u2212b \u00B1 \u221AΔ) / (2a)")
                        out.add("  = (${num(-B)} \u00B1 ${num(sqrt(d))}) / ${num(2 * A)}")
                        val x1 = (-B + sqrt(d)) / (2 * A)
                        val x2 = (-B - sqrt(d)) / (2 * A)
                        out.add("x\u2081 = ${num(x1)}")
                        out.add("x\u2082 = ${num(x2)}")
                    }
                    abs(d) < 1e-9 -> {
                        out.add("Δ = 0，有重根")
                        out.add("x = \u2212b / (2a) = ${num(-B / (2 * A))}")
                    }
                    else -> {
                        out.add("Δ < 0，无实根")
                    }
                }
            }
            else -> {
                out.add("${deg} 次方程")
                out.add("使用数值法求根")
            }
        }
        return out
    }

    fun num(v: Double): String {
        val r = v.roundToLong()
        return if (abs(v - r.toDouble()) < 1e-9) r.toString()
        else {
            val s = String.format("%.4f", v)
            s.trimEnd('0').trimEnd('.')
        }
    }

    private fun formatPoly(c: DoubleArray, deg: Int): String {
        val sb = StringBuilder()
        for (i in deg downTo 0) {
            val v = c.getOrElse(i) { 0.0 }
            if (abs(v) < 1e-12) continue
            if (sb.isNotEmpty()) sb.append(if (v < 0) " \u2212 " else " + ")
            else if (v < 0) sb.append("\u2212")
            val absV = abs(v)
            if (i == 0) {
                sb.append(num(absV))
            } else {
                if (abs(absV - 1.0) > 1e-9) sb.append(num(absV))
                sb.append("x")
                if (i == 2) sb.append("\u00B2")
                else if (i == 3) sb.append("\u00B3")
                else if (i > 1) sb.append("^$i")
            }
        }
        if (sb.isEmpty()) sb.append("0")
        return sb.toString()
    }

    /**
     * 通用数值求根
     *
     * 关键修复：把 prevF / fx 先赋给不可变 val，避免 Kotlin 无法智能转换
     */
    fun scanRoots(
        f: (Double) -> Double?,
        xMin: Double,
        xMax: Double
    ): List<Double> {
        val out = ArrayList<Double>()
        val samples = 2000
        var prevX = xMin
        var prevFv: Double? = f(xMin)

        for (i in 1..samples) {
            val x = xMin + (xMax - xMin) * i / samples
            val fxv: Double? = f(x)

            // ★ 先把可空值存到 val，Kotlin 才能安全智能转换为 Double
            val pF: Double? = prevFv
            val cF: Double? = fxv

            if (pF != null && cF != null) {
                if (abs(pF) < 1e-9) {
                    if (out.none { abs(it - prevX) < 1e-4 }) out.add(prevX)
                } else if (pF * cF < 0.0) {
                    var lo = prevX
                    var hi = x
                    var flo: Double = pF
                    repeat(50) {
                        val mid = (lo + hi) / 2
                        val fm: Double? = f(mid)
                        if (fm != null) {
                            if (flo * fm <= 0.0) {
                                hi = mid
                            } else {
                                lo = mid
                                flo = fm
                            }
                        }
                    }
                    val root = (lo + hi) / 2
                    if (out.none { abs(it - root) < 1e-4 }) out.add(root)
                }
            }

            prevX = x
            prevFv = fxv
        }
        return out
    }
}