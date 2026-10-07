package com.tengwear.jisuanqi.logic

import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sqrt

data class InequalityResult(
    val solution: String,
    val interval: String,
    val steps: List<String>,
    val error: String?
)

object InequalitySolver {

    fun num(v: Double): String {
        val r = v.roundToLong()
        return if (abs(v - r.toDouble()) < 1e-9) r.toString()
        else {
            val s = String.format("%.4f", v)
            s.trimEnd('0').trimEnd('.')
        }
    }

    fun solve(input: String): InequalityResult {
        val parsed = parseInequality(input)
            ?: return InequalityResult("", "", emptyList(), "缺少不等号或格式错误")
        val (lhs, op, rhs) = parsed

        val fExpr = "($lhs)-($rhs)"

        val poly = PolynomialSolver.fit(fExpr)
        if (poly != null) {
            return solvePolynomial(poly, op)
        }

        val rational = RationalSolver.trySplit(fExpr)
        if (rational != null) {
            return solveRational(rational, op)
        }

        return InequalityResult(
            "", "", emptyList(),
            "仅支持多项式或分式不等式"
        )
    }

    // ============ 供不等式组使用的区间集求解 ============

    fun solveInequalityAsSet(lhs: String, op: String, rhs: String): IntervalSet {
        val fExpr = "($lhs)-($rhs)"

        val poly = PolynomialSolver.fit(fExpr)
        if (poly != null) {
            return solvePolyAsSet(poly.coeffs, op)
        }

        val rat = RationalSolver.trySplit(fExpr)
        if (rat != null) {
            return solveRatAsSet(rat.numCoeffs, rat.denCoeffs, op)
        }

        return IntervalSet.empty()
    }

    private fun valueSatisfies(v: Double, op: String): Boolean = when (op) {
        ">" -> v > 1e-9
        "\u2265" -> v > -1e-9
        "<" -> v < -1e-9
        "\u2264" -> v < 1e-9
        else -> false
    }

    private fun evalPolyAt(c: DoubleArray, x: Double): Double {
        var v = 0.0
        for (i in c.indices.reversed()) v = v * x + c[i]
        return v
    }

    private fun rootsOfPoly(c: DoubleArray): List<Double> {
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

    private fun solvePolyAsSet(coeffs: DoubleArray, op: String): IntervalSet {
        var deg = coeffs.size - 1
        while (deg > 0 && abs(coeffs[deg]) < 1e-12) deg--

        if (deg == 0) {
            return if (valueSatisfies(coeffs[0], op)) IntervalSet.all()
            else IntervalSet.empty()
        }

        val roots = rootsOfPoly(coeffs).distinct().sorted()
        if (roots.isEmpty()) {
            val v = evalPolyAt(coeffs, 1.0)
            return if (valueSatisfies(v, op)) IntervalSet.all()
            else IntervalSet.empty()
        }

        return buildSetFromPoints(roots, op) { x -> evalPolyAt(coeffs, x) }
    }

    private fun solveRatAsSet(numC: DoubleArray, denC: DoubleArray, op: String): IntervalSet {
        val numRoots = rootsOfPoly(numC).distinct().sorted()
        val denRoots = rootsOfPoly(denC).distinct().sorted()
        val critical = (numRoots + denRoots).distinct().sorted()

        fun evalAt(x: Double): Double {
            val n = evalPolyAt(numC, x)
            val d = evalPolyAt(denC, x)
            return if (abs(d) < 1e-12) Double.NaN else n / d
        }

        if (critical.isEmpty()) {
            val v = evalAt(1.0)
            return if (v.isFinite() && valueSatisfies(v, op)) IntervalSet.all()
            else IntervalSet.empty()
        }

        return buildSetFromPoints(critical, op, ::evalAt)
    }

    private fun buildSetFromPoints(
        critical: List<Double>,
        op: String,
        evalAt: (Double) -> Double
    ): IntervalSet {
        val n = critical.size
        val negInf = Double.NEGATIVE_INFINITY
        val posInf = Double.POSITIVE_INFINITY

        val openIntervals = ArrayList<Pair<Double, Double>>()
        openIntervals.add(negInf to critical[0])
        for (i in 0 until n - 1) openIntervals.add(critical[i] to critical[i + 1])
        openIntervals.add(critical[n - 1] to posInf)

        val intervalOk = BooleanArray(openIntervals.size)
        for (i in openIntervals.indices) {
            val (a, b) = openIntervals[i]
            val test = when {
                a == negInf && b == posInf -> 1.0
                a == negInf -> b - 1.0
                b == posInf -> a + 1.0
                else -> (a + b) / 2.0
            }
            val v = evalAt(test)
            intervalOk[i] = v.isFinite() && valueSatisfies(v, op)
        }

        val pointOk = BooleanArray(n)
        for (i in 0 until n) {
            val v = evalAt(critical[i])
            pointOk[i] = v.isFinite() && valueSatisfies(v, op)
        }

        val units = ArrayList<Interval>()

        if (intervalOk[0]) {
            units.add(Interval(null, critical[0], false, false))
        }
        for (k in 0 until n) {
            if (pointOk[k]) {
                units.add(Interval(critical[k], critical[k], true, true))
            }
            val (a, b) = openIntervals[k + 1]
            if (intervalOk[k + 1]) {
                units.add(Interval(
                    if (a == negInf) null else a,
                    if (b == posInf) null else b,
                    false, false
                ))
            }
        }

        return IntervalSet(mergeAdjacent(units))
    }

    private fun mergeAdjacent(units: List<Interval>): List<Interval> {
        if (units.isEmpty()) return emptyList()
        val out = ArrayList<Interval>()
        var cur = units[0]
        for (i in 1 until units.size) {
            val nxt = units[i]
            val canMerge: Boolean = when {
                cur.right == null || nxt.left == null -> true
                cur.right!! > nxt.left!! + 1e-12 -> true
                abs(cur.right!! - nxt.left!!) < 1e-12 -> cur.rightClosed || nxt.leftClosed
                else -> false
            }
            cur = if (canMerge) {
                val newRight: Double?
                val newRightClosed: Boolean
                when {
                    cur.right == null || nxt.right == null -> {
                        newRight = null; newRightClosed = false
                    }
                    cur.right!! > nxt.right!! + 1e-12 -> {
                        newRight = cur.right; newRightClosed = cur.rightClosed
                    }
                    nxt.right!! > cur.right!! + 1e-12 -> {
                        newRight = nxt.right; newRightClosed = nxt.rightClosed
                    }
                    else -> {
                        newRight = cur.right
                        newRightClosed = cur.rightClosed || nxt.rightClosed
                    }
                }
                Interval(cur.left, newRight, cur.leftClosed, newRightClosed)
            } else {
                out.add(cur)
                nxt
            }
        }
        out.add(cur)
        return out
    }

    // ============ 主流程：多项式 ============

    private fun solvePolynomial(poly: PolyCoeffs, op: String): InequalityResult {
        val coeffs = poly.coeffs
        var deg = coeffs.size - 1
        while (deg > 0 && abs(coeffs[deg]) < 1e-12) deg--

        val steps = ArrayList<String>()
        steps.add("整理为：${formatPoly(coeffs, deg)} $op 0")

        if (deg == 0) {
            val v = coeffs[0]
            val satisfied = when (op) {
                ">", "\u2265" -> v > 1e-12 || (op == "\u2265" && abs(v) < 1e-12)
                "<", "\u2264" -> v < -1e-12 || (op == "\u2264" && abs(v) < 1e-12)
                else -> false
            }
            return if (satisfied) {
                steps.add("常数不等式恒成立")
                InequalityResult("全体实数", "(-\u221E, +\u221E)", steps, null)
            } else {
                steps.add("常数不等式不成立")
                InequalityResult("无解", "\u2205", steps, null)
            }
        }

        return when (deg) {
            1 -> solveLinear(coeffs[1], coeffs[0], op, steps)
            2 -> solveQuadratic(coeffs[2], coeffs[1], coeffs[0], op, steps)
            else -> InequalityResult("", "", steps, "仅支持一次和二次不等式")
        }
    }

    private data class Parsed(val lhs: String, val op: String, val rhs: String)

    private fun parseInequality(input: String): Parsed? {
        val ops = listOf("\u2264", "\u2265", "<=", ">=", "<", ">")
        for (op in ops) {
            val idx = input.indexOf(op)
            if (idx > 0) {
                val lhs = input.substring(0, idx).trim()
                val rhs = input.substring(idx + op.length).trim()
                if (lhs.isNotEmpty() && rhs.isNotEmpty()) {
                    val normOp = when (op) {
                        "<=" -> "\u2264"
                        ">=" -> "\u2265"
                        else -> op
                    }
                    return Parsed(lhs, normOp, rhs)
                }
            }
        }
        return null
    }

    private fun solveLinear(
        a: Double, b: Double, op: String, steps: ArrayList<String>
    ): InequalityResult {
        val x0 = -b / a
        steps.add("一元一次不等式")
        steps.add("  a = ${num(a)}，b = ${num(b)}")
        steps.add("零点：x = ${num(x0)}")

        val result: Pair<String, String> = if (a > 0) {
            when (op) {
                ">" -> "x > ${num(x0)}" to "(${num(x0)}, +\u221E)"
                "\u2265" -> "x \u2265 ${num(x0)}" to "[${num(x0)}, +\u221E)"
                "<" -> "x < ${num(x0)}" to "(-\u221E, ${num(x0)})"
                "\u2264" -> "x \u2264 ${num(x0)}" to "(-\u221E, ${num(x0)}]"
                else -> "" to ""
            }
        } else {
            when (op) {
                ">" -> "x < ${num(x0)}" to "(-\u221E, ${num(x0)})"
                "\u2265" -> "x \u2264 ${num(x0)}" to "(-\u221E, ${num(x0)}]"
                "<" -> "x > ${num(x0)}" to "(${num(x0)}, +\u221E)"
                "\u2264" -> "x \u2265 ${num(x0)}" to "[${num(x0)}, +\u221E)"
                else -> "" to ""
            }
        }

        steps.add("因为 a ${if (a > 0) ">" else "<"} 0，函数单调${if (a > 0) "递增" else "递减"}")
        steps.add("解集：${result.first}")

        return InequalityResult(result.first, result.second, steps, null)
    }

    private fun solveQuadratic(
        a: Double, b: Double, c: Double, op: String, steps: ArrayList<String>
    ): InequalityResult {
        val delta = b * b - 4 * a * c
        steps.add("一元二次不等式")
        steps.add("  a = ${num(a)}，b = ${num(b)}，c = ${num(c)}")
        steps.add("判别式 Δ = b\u00B2 \u2212 4ac = ${num(delta)}")

        val isGreater = op == ">" || op == "\u2265"

        if (delta < -1e-9) {
            steps.add("Δ < 0，无实根")
            return if (a > 0) {
                if (isGreater) {
                    steps.add("抛物线开口向上，f(x) 恒 > 0")
                    InequalityResult("全体实数", "(-\u221E, +\u221E)", steps, null)
                } else {
                    steps.add("抛物线开口向上，f(x) 恒 > 0，不满足")
                    InequalityResult("无解", "\u2205", steps, null)
                }
            } else {
                if (!isGreater) {
                    steps.add("抛物线开口向下，f(x) 恒 < 0")
                    InequalityResult("全体实数", "(-\u221E, +\u221E)", steps, null)
                } else {
                    steps.add("抛物线开口向下，f(x) 恒 < 0，不满足")
                    InequalityResult("无解", "\u2205", steps, null)
                }
            }
        }

        if (abs(delta) < 1e-9) {
            val x0 = -b / (2 * a)
            steps.add("Δ = 0，重根 x = ${num(x0)}")

            return if (a > 0) {
                when (op) {
                    ">" -> {
                        steps.add("f(x) > 0 除了 x = ${num(x0)}")
                        InequalityResult(
                            "x \u2260 ${num(x0)}",
                            "(-\u221E, ${num(x0)}) \u222A (${num(x0)}, +\u221E)",
                            steps, null
                        )
                    }
                    "\u2265" -> {
                        steps.add("f(x) \u2265 0 恒成立")
                        InequalityResult("全体实数", "(-\u221E, +\u221E)", steps, null)
                    }
                    "<" -> {
                        steps.add("f(x) < 0 恒不成立")
                        InequalityResult("无解", "\u2205", steps, null)
                    }
                    else -> {
                        steps.add("f(x) \u2264 0 只在 x = ${num(x0)} 成立")
                        InequalityResult("x = ${num(x0)}", "{${num(x0)}}", steps, null)
                    }
                }
            } else {
                when (op) {
                    "<" -> {
                        steps.add("f(x) < 0 除了 x = ${num(x0)}")
                        InequalityResult(
                            "x \u2260 ${num(x0)}",
                            "(-\u221E, ${num(x0)}) \u222A (${num(x0)}, +\u221E)",
                            steps, null
                        )
                    }
                    "\u2264" -> {
                        steps.add("f(x) \u2264 0 恒成立")
                        InequalityResult("全体实数", "(-\u221E, +\u221E)", steps, null)
                    }
                    ">" -> {
                        steps.add("f(x) > 0 恒不成立")
                        InequalityResult("无解", "\u2205", steps, null)
                    }
                    else -> {
                        steps.add("f(x) \u2265 0 只在 x = ${num(x0)} 成立")
                        InequalityResult("x = ${num(x0)}", "{${num(x0)}}", steps, null)
                    }
                }
            }
        }

        val sq = sqrt(delta)
        var x1 = (-b - sq) / (2 * a)
        var x2 = (-b + sq) / (2 * a)
        if (x1 > x2) { val t = x1; x1 = x2; x2 = t }

        steps.add("Δ > 0，两根 x\u2081 = ${num(x1)}，x\u2082 = ${num(x2)}")

        val x1s = num(x1)
        val x2s = num(x2)

        return if (a > 0) {
            when (op) {
                ">" -> {
                    steps.add("开口向上，f(x) > 0 在两根之外")
                    InequalityResult("x < $x1s 或 x > $x2s",
                        "(-\u221E, $x1s) \u222A ($x2s, +\u221E)", steps, null)
                }
                "\u2265" -> {
                    steps.add("开口向上，f(x) \u2265 0 在两根之外（含端点）")
                    InequalityResult("x \u2264 $x1s 或 x \u2265 $x2s",
                        "(-\u221E, $x1s] \u222A [$x2s, +\u221E)", steps, null)
                }
                "<" -> {
                    steps.add("开口向上，f(x) < 0 在两根之间")
                    InequalityResult("$x1s < x < $x2s", "($x1s, $x2s)", steps, null)
                }
                else -> {
                    steps.add("开口向上，f(x) \u2264 0 在两根之间（含端点）")
                    InequalityResult("$x1s \u2264 x \u2264 $x2s",
                        "[$x1s, $x2s]", steps, null)
                }
            }
        } else {
            when (op) {
                "<" -> {
                    steps.add("开口向下，f(x) < 0 在两根之外")
                    InequalityResult("x < $x1s 或 x > $x2s",
                        "(-\u221E, $x1s) \u222A ($x2s, +\u221E)", steps, null)
                }
                "\u2264" -> {
                    steps.add("开口向下，f(x) \u2264 0 在两根之外（含端点）")
                    InequalityResult("x \u2264 $x1s 或 x \u2265 $x2s",
                        "(-\u221E, $x1s] \u222A [$x2s, +\u221E)", steps, null)
                }
                ">" -> {
                    steps.add("开口向下，f(x) > 0 在两根之间")
                    InequalityResult("$x1s < x < $x2s", "($x1s, $x2s)", steps, null)
                }
                else -> {
                    steps.add("开口向下，f(x) \u2265 0 在两根之间（含端点）")
                    InequalityResult("$x1s \u2264 x \u2264 $x2s",
                        "[$x1s, $x2s]", steps, null)
                }
            }
        }
    }

    // ============ 主流程：分式 ============

    private fun solveRational(
        r: RationalSolver.Rational,
        op: String
    ): InequalityResult {
        val steps = ArrayList<String>()
        steps.add("分式不等式")
        steps.add("分子 P(x) = ${formatPoly(r.numCoeffs, r.numCoeffs.size - 1)}")
        steps.add("分母 Q(x) = ${formatPoly(r.denCoeffs, r.denCoeffs.size - 1)}")

        val numRoots = rootsOfPoly(r.numCoeffs)
        val denRoots = rootsOfPoly(r.denCoeffs)

        steps.add("分子零点：${if (numRoots.isEmpty()) "无" else numRoots.joinToString { num(it) }}")
        steps.add("分母零点（间断点）：${if (denRoots.isEmpty()) "无" else denRoots.joinToString { num(it) }}")

        val critical = (numRoots + denRoots).distinct().sorted()

        fun evalAt(x: Double): Double {
            val n = evalPolyAt(r.numCoeffs, x)
            val d = evalPolyAt(r.denCoeffs, x)
            return if (abs(d) < 1e-12) Double.NaN else n / d
        }

        if (critical.isEmpty()) {
            val v = evalAt(0.0)
            val satisfied = v.isFinite() && valueSatisfies(v, op)
            return if (satisfied) {
                steps.add("全程恒成立")
                InequalityResult("全体实数", "(-\u221E, +\u221E)", steps, null)
            } else {
                steps.add("全程不成立")
                InequalityResult("无解", "\u2205", steps, null)
            }
        }

        val set = buildSetFromPoints(critical, op, ::evalAt)
        val human = set.toHumanReadable()
        val symbolic = set.toSymbolic()

        if (set.isEmpty()) {
            return InequalityResult("无解", "\u2205", steps, null)
        }

        steps.add("解集：$human")
        return InequalityResult(human, symbolic, steps, null)
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
}