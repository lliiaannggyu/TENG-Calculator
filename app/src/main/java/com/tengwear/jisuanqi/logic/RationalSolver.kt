package com.tengwear.jisuanqi.logic

import kotlin.math.abs

/**
 * 分式识别器
 *
 * 尝试把表达式拆成 P(x)/Q(x)，其中 P、Q 都是多项式
 */
object RationalSolver {

    data class Rational(
        val numCoeffs: DoubleArray,
        val denCoeffs: DoubleArray
    )

    fun trySplit(fExpr: String): Rational? {
        val expr = stripOuterParens(fExpr.trim())
        val slashIdx = findTopLevelSlash(expr)
        if (slashIdx < 0) return null

        val numStr = stripOuterParens(expr.substring(0, slashIdx).trim())
        val denStr = stripOuterParens(expr.substring(slashIdx + 1).trim())

        if (numStr.isEmpty() || denStr.isEmpty()) return null

        val numPoly = PolynomialSolver.fit(numStr, maxDeg = 4) ?: return null
        val denPoly = PolynomialSolver.fit(denStr, maxDeg = 4) ?: return null

        if (denPoly.coeffs.all { abs(it) < 1e-12 }) return null

        return Rational(numPoly.coeffs, denPoly.coeffs)
    }

    private fun stripOuterParens(s: String): String {
        var str = s.trim()
        while (str.length >= 2 && str.startsWith("(") && str.endsWith(")")) {
            var depth = 0
            var ok = true
            for (i in str.indices) {
                when (str[i]) {
                    '(' -> depth++
                    ')' -> {
                        depth--
                        if (depth == 0 && i < str.length - 1) {
                            ok = false
                            break
                        }
                    }
                }
            }
            if (ok) str = str.substring(1, str.length - 1).trim() else break
        }
        return str
    }

    private fun findTopLevelSlash(s: String): Int {
        var depth = 0
        var idx = -1
        for (i in s.indices) {
            when (s[i]) {
                '(' -> depth++
                ')' -> depth--
                '/' -> if (depth == 0) idx = i
            }
        }
        return idx
    }
}