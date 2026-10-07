package com.tengwear.jisuanqi.logic

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * 任意精度的有理数（分数）表示。
 */
data class Rational private constructor(
    val num: BigInteger,
    val den: BigInteger
) {
    companion object {
        val ZERO: Rational = Rational(BigInteger.ZERO, BigInteger.ONE)
        val ONE: Rational = Rational(BigInteger.ONE, BigInteger.ONE)

        fun of(num: BigInteger, den: BigInteger): Rational {
            require(den.signum() != 0) { "分母不能为 0" }
            var n = num
            var d = den
            if (d.signum() < 0) { n = n.negate(); d = d.negate() }
            if (n.signum() == 0) return ZERO
            val g = n.gcd(d)
            if (g.signum() == 0) return ZERO
            return Rational(n.divide(g), d.divide(g))
        }

        fun ofLong(v: Long): Rational = Rational(BigInteger.valueOf(v), BigInteger.ONE)
        fun ofBigInteger(v: BigInteger): Rational = Rational(v, BigInteger.ONE)

        /** 从字符串解析：支持整数、小数、可选负号、科学计数法（1.5E10） */
        fun parse(s: String): Rational? {
            val str = s.trim()
            if (str.isEmpty()) return null

            if (str.contains('E') || str.contains('e')) {
                return try {
                    val bd = BigDecimal(str)
                    val unscaled = bd.unscaledValue()
                    val scale = bd.scale()
                    if (scale >= 0) of(unscaled, BigInteger.TEN.pow(scale))
                    else of(unscaled.multiply(BigInteger.TEN.pow(-scale)), BigInteger.ONE)
                } catch (_: Exception) { null }
            }

            val dot = str.indexOf('.')
            if (dot < 0) {
                return try { ofBigInteger(str.toBigInteger()) } catch (_: Exception) { null }
            }

            val intPart = str.substring(0, dot)
            val fracPart = str.substring(dot + 1)
            if (fracPart.isEmpty() && intPart.isEmpty()) return null

            val sign = if (intPart.startsWith("-")) -1 else 1
            val absInt = intPart.removePrefix("-").removePrefix("+").ifEmpty { "0" }
            if (!absInt.all { it.isDigit() } || !fracPart.all { it.isDigit() }) return null

            val digits = absInt + fracPart
            val num = try { digits.toBigInteger() } catch (_: Exception) { return null }
            val den = BigInteger.TEN.pow(fracPart.length)
            return of(num.multiply(BigInteger.valueOf(sign.toLong())), den)
        }
    }

    operator fun plus(o: Rational): Rational =
        of(num.multiply(o.den).add(o.num.multiply(den)), den.multiply(o.den))

    operator fun minus(o: Rational): Rational =
        of(num.multiply(o.den).subtract(o.num.multiply(den)), den.multiply(o.den))

    operator fun times(o: Rational): Rational =
        of(num.multiply(o.num), den.multiply(o.den))

    operator fun div(o: Rational): Rational {
        require(o.num.signum() != 0) { "除数不能为 0" }
        return of(num.multiply(o.den), den.multiply(o.num))
    }

    fun negate(): Rational = Rational(num.negate(), den)
    fun abs(): Rational = if (num.signum() < 0) Rational(num.negate(), den) else this
    fun signum(): Int = num.signum()
    fun isZero(): Boolean = num.signum() == 0
    fun isInteger(): Boolean = den == BigInteger.ONE

    fun toBigIntegerExact(): BigInteger {
        require(den == BigInteger.ONE) { "非整数" }
        return num
    }

    /** 幂（整数指数） */
    fun pow(n: Int): Rational {
        if (n == 0) return ONE
        val absN = if (n < 0) -n else n
        val p = num.pow(absN)
        val q = den.pow(absN)
        return if (n > 0) of(p, q) else of(q, p)
    }

    /** 转 BigDecimal（近似，用于无理场景） */
    fun toBigDecimal(scale: Int, mode: RoundingMode = RoundingMode.HALF_UP): BigDecimal =
        BigDecimal(num).divide(BigDecimal(den), scale, mode)

    fun toDouble(): Double = num.toDouble() / den.toDouble()

    /** 精确整数的 n 次根（若存在），否则 null */
    fun nthRootExact(n: Int): Rational? {
        if (n <= 0) return null
        if (signum() < 0 && n % 2 == 0) return null
        val numRoot = num.abs().nthRootExactBigInt(n) ?: return null
        val denRoot = den.nthRootExactBigInt(n) ?: return null
        val r = of(numRoot, denRoot)
        return if (signum() < 0) r.negate() else r
    }
}

/** BigInteger 的整数 n 次根，若精确则返回根，否则 null */
internal fun BigInteger.nthRootExactBigInt(n: Int): BigInteger? {
    if (n <= 0 || signum() < 0) return null
    if (n == 1) return this
    if (signum() == 0) return BigInteger.ZERO
    if (this == BigInteger.ONE) return BigInteger.ONE
    val root = nthRootFloorBigInt(n)
    return if (root.pow(n) == this) root else null
}

/** BigInteger 的整数 n 次根向下取整（牛顿法） */
internal fun BigInteger.nthRootFloorBigInt(n: Int): BigInteger {
    require(n >= 1)
    if (n == 1) return this
    require(signum() >= 0)
    if (signum() == 0) return BigInteger.ZERO
    if (this == BigInteger.ONE) return BigInteger.ONE

    val nBig = BigInteger.valueOf(n.toLong())
    val nMinus1 = nBig - BigInteger.ONE
    // ★ 修复：bitLength() 是方法，不是属性
    var x = BigInteger.ONE.shiftLeft((bitLength() + n - 1) / n)
    if (x.signum() == 0) x = BigInteger.ONE

    var iter = 0
    while (iter < 100000) {
        val xPow = x.pow(n - 1)
        if (xPow.signum() == 0) break
        val next = nMinus1.multiply(x).add(this.divide(xPow)).divide(nBig)
        if (next >= x) break
        x = next
        iter++
    }
    while (x.pow(n) > this) x = x.subtract(BigInteger.ONE)
    while (x.add(BigInteger.ONE).pow(n) <= this) x = x.add(BigInteger.ONE)
    return x
}