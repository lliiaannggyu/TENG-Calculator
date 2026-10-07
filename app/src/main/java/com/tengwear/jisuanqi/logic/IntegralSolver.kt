package com.tengwear.jisuanqi.logic

import kotlin.math.abs
import kotlin.math.roundToLong

data class IntegralResult(
    val value: Double?,
    val steps: List<String>,
    val error: String?
)

object IntegralSolver {

    fun solve(expr: String, a: Double, b: Double): IntegralResult {
        val steps = ArrayList<String>()

        if (!a.isFinite() || !b.isFinite()) {
            return IntegralResult(null, steps, "端点无效")
        }

        if (abs(a - b) < 1e-12) {
            steps.add("积分区间为零")
            return IntegralResult(0.0, steps, null)
        }

        val swap = a > b
        val lo = if (swap) b else a
        val hi = if (swap) a else b

        val n = 2000
        val h = (hi - lo) / n

        val f0 = ExpressionEvaluator.evaluate(expr, lo)
        val fn = ExpressionEvaluator.evaluate(expr, hi)
        if (f0 == null || fn == null) {
            return IntegralResult(null, steps, "端点处函数无定义")
        }

        var sum = f0 + fn
        var invalidCount = 0

        for (i in 1 until n) {
            val x = lo + i * h
            val y = ExpressionEvaluator.evaluate(expr, x)
            if (y == null || !y.isFinite()) {
                invalidCount++
                continue
            }
            val coef = if (i % 2 == 0) 2.0 else 4.0
            sum += coef * y
        }

        if (invalidCount > n / 10) {
            return IntegralResult(
                null, steps,
                "函数在区间内 ${invalidCount * 100 / n}% 的采样点无定义（可能有奇点）"
            )
        }

        val value = sum * h / 3.0
        val result = if (swap) -value else value

        steps.add("被积函数：f(x) = $expr")
        steps.add("积分区间：[$lo, $hi]")
        steps.add("数值方法：复合辛普森法")
        steps.add("  区间数 n = $n")
        steps.add("  步长 h = ${formatNum(h)}")
        if (invalidCount > 0) {
            steps.add("  （跳过 $invalidCount 个无定义点）")
        }
        if (swap) {
            steps.add("（交换上下限，结果取反）")
        }

        return IntegralResult(result, steps, null)
    }

    fun formatNum(v: Double): String {
        if (!v.isFinite()) return v.toString()
        val r = v.roundToLong()
        return if (abs(v - r.toDouble()) < 1e-9) r.toString()
        else {
            val s = String.format("%.6f", v)
            s.trimEnd('0').trimEnd('.')
        }
    }
}