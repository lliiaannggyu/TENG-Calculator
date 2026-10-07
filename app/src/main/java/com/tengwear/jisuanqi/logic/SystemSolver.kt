package com.tengwear.jisuanqi.logic

import kotlin.math.abs
import kotlin.math.roundToLong

data class SystemResult(
    val variables: List<String>,
    val values: List<Double>,
    val steps: List<String>,
    val error: String?
)

object SystemSolver {

    /**
     * 自动检测变量名（按 x, y, z 顺序）
     */
    fun detectVariables(equations: List<String>): List<String> {
        val found = mutableSetOf<String>()
        for (eq in equations) {
            var i = 0
            while (i < eq.length) {
                if (eq[i].isLetter()) {
                    val start = i
                    while (i < eq.length && eq[i].isLetterOrDigit()) i++
                    val ident = eq.substring(start, i).lowercase()
                    if (ident == "x" || ident == "y" || ident == "z") {
                        found.add(ident)
                    }
                } else {
                    i++
                }
            }
        }
        return listOf("x", "y", "z").filter { it in found }
    }

    /**
     * 求解线性方程组
     */
    fun solve(equations: List<String>): SystemResult {
        val eqs = equations.filter { it.isNotBlank() }
        if (eqs.isEmpty()) {
            return SystemResult(emptyList(), emptyList(), emptyList(), "无方程")
        }

        val vars = detectVariables(eqs)
        if (vars.isEmpty()) {
            return SystemResult(emptyList(), emptyList(), emptyList(), "未找到变量 x/y/z")
        }

        val n = vars.size
        if (eqs.size != n) {
            return SystemResult(
                vars, emptyList(), emptyList(),
                "方程数(${eqs.size}) ≠ 变量数($n)"
            )
        }

        // 每个方程对变量的系数
        val A = Array(n) { DoubleArray(n) }
        val B = DoubleArray(n)

        val zeros = vars.associateWith { 0.0 }

        for (i in eqs.indices) {
            val eq = eqs[i]
            val idx = eq.indexOf('=')
            if (idx < 0) {
                return SystemResult(vars, emptyList(), emptyList(), "方程 ${i + 1} 缺少等号")
            }
            val lhs = eq.substring(0, idx)
            val rhs = eq.substring(idx + 1)

            val f = { m: Map<String, Double> ->
                val l = ExpressionEvaluator.evaluateVars(lhs, m)
                val r = ExpressionEvaluator.evaluateVars(rhs, m)
                if (l == null || r == null) null else l - r
            }

            val f0 = f(zeros)
                ?: return SystemResult(vars, emptyList(), emptyList(), "方程 ${i + 1} 无法求值")

            // 移项：A x + B y + C z = -f0
            B[i] = -f0

            for (j in vars.indices) {
                val v1 = vars.associateWith { if (it == vars[j]) 1.0 else 0.0 }
                val f1 = f(v1)
                    ?: return SystemResult(vars, emptyList(), emptyList(), "方程 ${i + 1} 无法求值")
                A[i][j] = f1 - f0
            }
        }

        // 高斯消元
        val x = gaussian(A, B)
            ?: return SystemResult(vars, emptyList(), emptyList(), "方程组无唯一解或无穷多解")

        // 生成步骤
        val steps = ArrayList<String>()
        steps.add("方程数 = 变量数 = $n")
        steps.add("使用高斯消元法")
        steps.add("")

        // 展示化简后的系数
        steps.add("系数矩阵：")
        for (i in 0 until n) {
            val sb = StringBuilder("  ")
            for (j in 0 until n) {
                if (j > 0) sb.append("  ")
                sb.append(num(A[i][j]))
            }
            sb.append("  |  ")
            sb.append(num(B[i]))
            steps.add(sb.toString())
        }

        return SystemResult(vars, x.toList(), steps, null)
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

    fun num(v: Double): String {
        val r = v.roundToLong()
        return if (abs(v - r.toDouble()) < 1e-9) r.toString()
        else {
            val s = String.format("%.4f", v)
            s.trimEnd('0').trimEnd('.')
        }
    }
}