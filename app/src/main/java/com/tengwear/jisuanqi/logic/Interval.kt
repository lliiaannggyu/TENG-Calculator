package com.tengwear.jisuanqi.logic

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * 单个区间，null 表示无穷
 */
data class Interval(
    val left: Double?,
    val right: Double?,
    val leftClosed: Boolean,
    val rightClosed: Boolean
) {
    fun isEmpty(): Boolean {
        if (left == null && right == null) return false
        if (left != null && right != null) {
            if (left > right + 1e-12) return true
            if (abs(left - right) < 1e-12) return !(leftClosed && rightClosed)
        }
        return false
    }
}

data class IntervalSet(val intervals: List<Interval>) {

    fun isEmpty(): Boolean = intervals.isEmpty()

    companion object {
        fun empty() = IntervalSet(emptyList())
        fun all() = IntervalSet(listOf(Interval(null, null, false, false)))
    }
}

fun Interval.intersect(other: Interval): Interval? {
    val newLeft: Double?
    val newLeftClosed: Boolean
    when {
        left == null -> {
            newLeft = other.left
            newLeftClosed = if (other.left == null) false else other.leftClosed
        }
        other.left == null -> {
            newLeft = left
            newLeftClosed = leftClosed
        }
        left > other.left + 1e-12 -> {
            newLeft = left
            newLeftClosed = leftClosed
        }
        other.left > left + 1e-12 -> {
            newLeft = other.left
            newLeftClosed = other.leftClosed
        }
        else -> {
            newLeft = left
            newLeftClosed = leftClosed && other.leftClosed
        }
    }

    val newRight: Double?
    val newRightClosed: Boolean
    when {
        right == null -> {
            newRight = other.right
            newRightClosed = if (other.right == null) false else other.rightClosed
        }
        other.right == null -> {
            newRight = right
            newRightClosed = rightClosed
        }
        right < other.right - 1e-12 -> {
            newRight = right
            newRightClosed = rightClosed
        }
        other.right < right - 1e-12 -> {
            newRight = other.right
            newRightClosed = other.rightClosed
        }
        else -> {
            newRight = right
            newRightClosed = rightClosed && other.rightClosed
        }
    }

    val r = Interval(newLeft, newRight, newLeftClosed, newRightClosed)
    return if (r.isEmpty()) null else r
}

fun IntervalSet.intersect(other: IntervalSet): IntervalSet {
    val out = ArrayList<Interval>()
    for (a in intervals) for (b in other.intervals) {
        val r = a.intersect(b)
        if (r != null) out.add(r)
    }
    return IntervalSet(out.sortedBy { it.left ?: Double.NEGATIVE_INFINITY }).normalize()
}

fun IntervalSet.normalize(): IntervalSet {
    if (intervals.size <= 1) return this
    val sorted = intervals.sortedBy { it.left ?: Double.NEGATIVE_INFINITY }
    val out = ArrayList<Interval>()
    var cur = sorted[0]
    for (i in 1 until sorted.size) {
        val nxt = sorted[i]
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
                    newRight = null
                    newRightClosed = false
                }
                cur.right!! > nxt.right!! + 1e-12 -> {
                    newRight = cur.right
                    newRightClosed = cur.rightClosed
                }
                nxt.right!! > cur.right!! + 1e-12 -> {
                    newRight = nxt.right
                    newRightClosed = nxt.rightClosed
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
    return IntervalSet(out)
}

private fun numOf(v: Double): String {
    val r = v.roundToLong()
    return if (abs(v - r.toDouble()) < 1e-9) r.toString()
    else {
        val s = String.format("%.4f", v)
        s.trimEnd('0').trimEnd('.')
    }
}

fun IntervalSet.toHumanReadable(): String {
    if (intervals.isEmpty()) return "无解"
    if (intervals.size == 1 && intervals[0].left == null && intervals[0].right == null) {
        return "全体实数"
    }
    return intervals.joinToString(" 或 ") { iv ->
        when {
            iv.left == null && iv.right == null -> "全体实数"
            iv.left == null -> {
                val op = if (iv.rightClosed) "\u2264" else "<"
                "x $op ${numOf(iv.right!!)}"
            }
            iv.right == null -> {
                val op = if (iv.leftClosed) "\u2265" else ">"
                "x $op ${numOf(iv.left!!)}"
            }
            else -> {
                val op1 = if (iv.leftClosed) "\u2264" else "<"
                val op2 = if (iv.rightClosed) "\u2264" else "<"
                "${numOf(iv.left)} $op1 x $op2 ${numOf(iv.right)}"
            }
        }
    }
}

fun IntervalSet.toSymbolic(): String {
    if (intervals.isEmpty()) return "\u2205"
    if (intervals.size == 1 && intervals[0].left == null && intervals[0].right == null) {
        return "(-\u221E, +\u221E)"
    }
    return intervals.joinToString(" \u222A ") { iv ->
        val lb = if (iv.leftClosed) "[" else "("
        val rb = if (iv.rightClosed) "]" else ")"
        val ls = if (iv.left == null) "-\u221E" else numOf(iv.left)
        val rs = if (iv.right == null) "+\u221E" else numOf(iv.right)
        "$lb$ls, $rs$rb"
    }
}