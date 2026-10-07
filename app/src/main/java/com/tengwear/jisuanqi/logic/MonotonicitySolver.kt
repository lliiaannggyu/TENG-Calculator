package com.tengwear.jisuanqi.logic

import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sqrt

data class MonoInterval(
    val left: Double?,
    val right: Double?,
    val increasing: Boolean
)

data class Extremum(
    val x: Double,
    val y: Double,
    val isMax: Boolean
)

data class MonotonicityResult(
    val intervals: List<MonoInterval>,
    val extrema: List<Extremum>,
    val derivativeDisplay: String,
    val steps: List<String>,
    val error: String?
)

object MonotonicitySolver {

    fun num(v: Double): String {
        val r = v.roundToLong()
        return if (abs(v - r.toDouble()) < 1e-9) r.toString()
        else {
            val s = String.format("%.4f", v)
            s.trimEnd('0').trimEnd('.')
        }
    }

    fun solve(expr: String): MonotonicityResult {
        val poly = PolynomialSolver.fit(expr, maxDeg = 4)
        if (poly != null) return solvePolynomial(poly)
        return solveNumeric(expr)
    }

    // ============ 多项式：符号求导 ============
    private fun solvePolynomial(poly: PolyCoeffs): MonotonicityResult {
        val c = poly.coeffs
        var deg = c.size - 1
        while (deg > 0 && abs(c[deg]) < 1e-12) deg--

        val steps = ArrayList<String>()
        steps.add("函数：y = ${formatPoly(c, deg)}")

        if (deg == 0) {
            steps.add("常函数，无单调性")
            return MonotonicityResult(emptyList(), emptyList(), "", steps, null)
        }

        val dc = derivativeCoeffs(c)
        var ddeg = dc.size - 1
        while (ddeg > 0 && abs(dc[ddeg]) < 1e-12) ddeg--

        steps.add("求导：y\u2032 = ${formatPoly(dc, ddeg)}")
        steps.add("")

        if (ddeg == 0) {
            val sign = dc[0]
            val increasing = sign > 0
            steps.add("y\u2032 是常数 ${num(sign)}")
            val iv = MonoInterval(null, null, increasing)
            return MonotonicityResult(
                listOf(iv), emptyList(),
                formatPoly(dc, ddeg), steps, null
            )
        }

        val criticalPoints = rootsOf(dc).distinct().sorted()

        steps.add("驻点（y\u2032 = 0）：${
            if (criticalPoints.isEmpty()) "无"
            else criticalPoints.joinToString { "x = ${num(it)}" }
        }")

        val intervals = ArrayList<MonoInterval>()
        val extrema = ArrayList<Extremum>()

        val evalD: (Double) -> Double = { x -> evalPolyAt(dc, x) }
        val evalF: (Double) -> Double = { x -> evalPolyAt(c, x) }

        val negInf = Double.NEGATIVE_INFINITY
        val posInf = Double.POSITIVE_INFINITY
        val points = listOf(negInf) + criticalPoints + listOf(posInf)

        steps.add("")
        steps.add("各区间 y\u2032 符号：")

        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            val test = when {
                a == negInf && b == posInf -> 0.0
                a == negInf -> b - 1.0
                b == posInf -> a + 1.0
                else -> (a + b) / 2.0
            }
            val dv = evalD(test)
            val inc = dv > 0
            intervals.add(MonoInterval(
                if (a == negInf) null else a,
                if (b == posInf) null else b,
                inc
            ))
            val display = when {
                a == negInf -> "(-\u221E, ${num(b)})"
                b == posInf -> "(${num(a)}, +\u221E)"
                else -> "(${num(a)}, ${num(b)})"
            }
            steps.add("  $display：y\u2032 ${if (dv > 0) "> 0" else "< 0"} \u2192 ${if (inc) "递增" else "递减"}")
        }

        for (i in criticalPoints.indices) {
            val x = criticalPoints[i]
            val y = evalF(x)
            val leftIdx = i
            val rightIdx = i + 1
            if (leftIdx < intervals.size && rightIdx < intervals.size) {
                val leftInc = intervals[leftIdx].increasing
                val rightInc = intervals[rightIdx].increasing
                when {
                    leftInc && !rightInc -> extrema.add(Extremum(x, y, true))
                    !leftInc && rightInc -> extrema.add(Extremum(x, y, false))
                }
            }
        }

        if (extrema.isNotEmpty()) {
            steps.add("")
            steps.add("极值点：")
            for (e in extrema) {
                val type = if (e.isMax) "极大值" else "极小值"
                steps.add("  x = ${num(e.x)} 时 $type y = ${num(e.y)}")
            }
        }

        return MonotonicityResult(
            intervals, extrema,
            formatPoly(dc, ddeg),
            steps, null
        )
    }

    // ============ 非多项式：数值微分 ============
    private fun solveNumeric(expr: String): MonotonicityResult {
        val steps = ArrayList<String>()
        steps.add("函数：y = $expr")
        steps.add("求导：使用数值微分")
        steps.add("")

        val h = 1e-5

        // 独立函数：明确返回 Double?，内部避免智能转换问题
        fun evalF(x: Double): Double? = ExpressionEvaluator.evaluate(expr, x)

        fun evalD(x: Double): Double? {
            val a: Double? = evalF(x + h)
            val b: Double? = evalF(x - h)
            if (a == null || b == null) return null
            val aV: Double = a
            val bV: Double = b
            return (aV - bV) / (2 * h)
        }

        val scanMin = -50.0
        val scanMax = 50.0
        val samples = 2000
        val critical = ArrayList<Double>()

        var prevX = scanMin
        var prevDv: Double? = evalD(scanMin)

        for (i in 1..samples) {
            val x = scanMin + (scanMax - scanMin) * i / samples
            val dv: Double? = evalD(x)

            val pD: Double? = prevDv
            val cD: Double? = dv

            if (pD != null && cD != null) {
                val pDv: Double = pD
                val cDv: Double = cD

                if (abs(pDv) < 1e-6) {
                    if (critical.none { abs(it - prevX) < 1e-3 }) critical.add(prevX)
                } else if (pDv * cDv < 0.0) {
                    var lo = prevX
                    var hi = x
                    var flo: Double = pDv
                    repeat(40) {
                        val mid = (lo + hi) / 2
                        val dm: Double? = evalD(mid)
                        if (dm != null) {
                            val dmV: Double = dm
                            if (flo * dmV <= 0.0) {
                                hi = mid
                            } else {
                                lo = mid
                                flo = dmV
                            }
                        }
                    }
                    val root = (lo + hi) / 2
                    if (critical.none { abs(it - root) < 1e-3 }) critical.add(root)
                }
            }

            prevX = x
            prevDv = dv
        }

        steps.add("扫描范围：[$scanMin, $scanMax]")
        steps.add("驻点：${
            if (critical.isEmpty()) "无"
            else critical.joinToString { "x = ${num(it)}" }
        }")

        val intervals = ArrayList<MonoInterval>()
        val extrema = ArrayList<Extremum>()
        val negInf = Double.NEGATIVE_INFINITY
        val posInf = Double.POSITIVE_INFINITY
        val points = listOf(negInf) + critical + listOf(posInf)

        steps.add("")
        steps.add("各区间导数符号：")

        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            val test = when {
                a == negInf && b == posInf -> 0.0
                a == negInf -> b - 0.5
                b == posInf -> a + 0.5
                else -> (a + b) / 2
            }
            val dvRaw: Double? = evalD(test)
            val dv: Double = dvRaw ?: 0.0
            val inc = dv > 0
            intervals.add(MonoInterval(
                if (a == negInf) null else a,
                if (b == posInf) null else b,
                inc
            ))
            val display = when {
                a == negInf -> "(-\u221E, ${num(b)})"
                b == posInf -> "(${num(a)}, +\u221E)"
                else -> "(${num(a)}, ${num(b)})"
            }
            steps.add("  $display：${if (inc) "递增" else "递减"}")
        }

        for (i in critical.indices) {
            val x = critical[i]
            val yRaw: Double? = evalF(x)
            val y: Double = yRaw ?: continue
            val leftIdx = i
            val rightIdx = i + 1
            if (leftIdx < intervals.size && rightIdx < intervals.size) {
                val leftInc = intervals[leftIdx].increasing
                val rightInc = intervals[rightIdx].increasing
                when {
                    leftInc && !rightInc -> extrema.add(Extremum(x, y, true))
                    !leftInc && rightInc -> extrema.add(Extremum(x, y, false))
                }
            }
        }

        if (extrema.isNotEmpty()) {
            steps.add("")
            steps.add("极值点：")
            for (e in extrema) {
                val type = if (e.isMax) "极大值" else "极小值"
                steps.add("  x = ${num(e.x)} 时 $type y = ${num(e.y)}")
            }
        }

        return MonotonicityResult(
            intervals, extrema,
            "数值微分",
            steps, null
        )
    }

    // ============ 工具 ============
    private fun derivativeCoeffs(c: DoubleArray): DoubleArray {
        if (c.size <= 1) return doubleArrayOf(0.0)
        val out = DoubleArray(c.size - 1)
        for (i in 1 until c.size) {
            out[i - 1] = c[i] * i
        }
        return out
    }

    private fun evalPolyAt(c: DoubleArray, x: Double): Double {
        var v = 0.0
        for (i in c.indices.reversed()) v = v * x + c[i]
        return v
    }

    private fun rootsOf(c: DoubleArray): List<Double> {
        var deg = c.size - 1
        while (deg > 0 && abs(c[deg]) < 1e-12) deg--
        if (deg <= 0) return emptyList()
        return when (deg) {
            1 -> listOf(-c[0] / c[1])
            2 -> {
                val A = c[2]; val B = c[1]; val C = c[0]
                val d = B * B - 4 * A * C
                when {
                    d < -1e-9 -> emptyList()
                    abs(d) < 1e-9 -> listOf(-B / (2 * A))
                    else -> {
                        val sq = sqrt(d)
                        listOf((-B + sq) / (2 * A), (-B - sq) / (2 * A))
                    }
                }
            }
            else -> PolynomialSolver.scanRoots(
                { x -> evalPolyAt(c, x) }, -100.0, 100.0
            )
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

    fun intervalDisplay(iv: MonoInterval): String {
        val l = if (iv.left == null) "-\u221E" else num(iv.left)
        val r = if (iv.right == null) "+\u221E" else num(iv.right)
        return "($l, $r)"
    }

    fun intervalDescription(iv: MonoInterval): String {
        val l = if (iv.left == null) "-\u221E" else num(iv.left)
        val r = if (iv.right == null) "+\u221E" else num(iv.right)
        val arrow = if (iv.increasing) "\u2197" else "\u2198"
        val word = if (iv.increasing) "递增" else "递减"
        return "$arrow ($l, $r) $word"
    }
}