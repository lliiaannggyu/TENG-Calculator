package com.tengwear.jisuanqi.logic

import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sqrt

data class DataPoint(val x: Double, val y: Double)

data class RegressionResult(
    val a: Double,          // 斜率
    val b: Double,          // 截距
    val r: Double,          // 相关系数
    val r2: Double,         // 决定系数 R²
    val n: Int,
    val meanX: Double,
    val meanY: Double,
    val equation: String,
    val steps: List<String>,
    val error: String?
)

object RegressionSolver {

    fun num(v: Double): String {
        val r = v.roundToLong()
        return if (abs(v - r.toDouble()) < 1e-9) r.toString()
        else {
            val s = String.format("%.6f", v)
            s.trimEnd('0').trimEnd('.')
        }
    }

    fun solve(points: List<DataPoint>): RegressionResult {
        if (points.size < 2) {
            return RegressionResult(
                0.0, 0.0, 0.0, 0.0, points.size,
                0.0, 0.0, "", emptyList(),
                "至少需要 2 个数据点"
            )
        }

        val n = points.size
        var sx = 0.0
        var sy = 0.0
        var sxx = 0.0
        var syy = 0.0
        var sxy = 0.0
        for (p in points) {
            sx += p.x
            sy += p.y
            sxx += p.x * p.x
            syy += p.y * p.y
            sxy += p.x * p.y
        }

        val denom = n * sxx - sx * sx
        if (abs(denom) < 1e-12) {
            return RegressionResult(
                0.0, 0.0, 0.0, 0.0, n,
                sx / n, sy / n, "", emptyList(),
                "所有 x 值相同，无法拟合直线"
            )
        }

        val a = (n * sxy - sx * sy) / denom
        val b = (sy - a * sx) / n

        val meanX = sx / n
        val meanY = sy / n

        // 相关系数 r
        val numR = n * sxy - sx * sy
        val denomR = (n * sxx - sx * sx) * (n * syy - sy * sy)
        val r = if (denomR > 1e-20) numR / sqrt(denomR) else 0.0
        val r2 = r * r

        val sign = if (b >= 0) "+" else "\u2212"
        val equation = "y = ${num(a)}x $sign ${num(abs(b))}"

        val steps = ArrayList<String>()
        steps.add("数据点数：n = $n")
        steps.add("")
        steps.add("Σx  = ${num(sx)}")
        steps.add("Σy  = ${num(sy)}")
        steps.add("Σx\u00B2 = ${num(sxx)}")
        steps.add("Σxy = ${num(sxy)}")
        steps.add("均值 x\u0304 = ${num(meanX)}, y\u0304 = ${num(meanY)}")
        steps.add("")
        steps.add("斜率 a = (n\u00B7Σxy \u2212 Σx\u00B7Σy) / (n\u00B7Σx\u00B2 \u2212 (Σx)\u00B2)")
        steps.add("      = (${num(n.toDouble())}\u00B7${num(sxy)} \u2212 ${num(sx)}\u00B7${num(sy)}) / (${num(n.toDouble())}\u00B7${num(sxx)} \u2212 ${num(sx)}\u00B2)")
        steps.add("      = ${num(a)}")
        steps.add("")
        steps.add("截距 b = (Σy \u2212 a\u00B7Σx) / n")
        steps.add("      = (${num(sy)} \u2212 ${num(a)}\u00B7${num(sx)}) / ${num(n.toDouble())}")
        steps.add("      = ${num(b)}")
        steps.add("")
        steps.add("相关系数 r = ${num(r)}")
        steps.add("决定系数 R\u00B2 = ${num(r2)}")
        steps.add("")
        steps.add("拟合方程：$equation")

        return RegressionResult(a, b, r, r2, n, meanX, meanY, equation, steps, null)
    }
}