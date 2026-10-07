package com.tengwear.jisuanqi.logic

/**
 * 元素化合价计算器。
 *
 * 输入化学式（如 H2SO4、KMnO4、Fe2O3），输出每个元素的化合价。
 * 推导思路：
 *  1. H → +1（暂不处理金属氢化物）
 *  2. 碱金属 → +1，碱土金属 → +2，Al → +3，Zn → +2，Ag → +1
 *  3. 若只剩一个未知元素，由电荷平衡 Σ(n×v)=0 直接求解
 *  4. 若还有多个未知，先固定 O = -2，再解剩下的
 *  5. 还有多个未知时，用 ValenceData 里的常见价态暴力枚举
 */
object ValenceCalculator {

    data class ElementValence(
        val symbol: String,
        val name: String,
        val count: Int,
        val valence: Int
    )

    data class Result(
        val formula: String,
        val elements: List<ElementValence>,
        val error: String? = null
    )

    fun calculate(formula: String): Result {
        val clean = formula.trim()
        if (clean.isEmpty()) return Result(formula, emptyList(), "请输入化学式")
        val parsed = parse(clean)
            ?: return Result(formula, emptyList(), "无法解析：请检查元素符号")
        if (parsed.isEmpty()) return Result(formula, emptyList(), "无法解析")

        // 单质：化合价为 0
        if (parsed.size == 1) {
            val (sym, cnt) = parsed.entries.first()
            return Result(formula, listOf(ElementValence(sym, nameOf(sym), cnt, 0)))
        }

        val solved = solve(parsed)
            ?: return Result(formula, emptyList(), "无法确定化合价组合")

        val list = parsed.entries.map { (sym, cnt) ->
            ElementValence(sym, nameOf(sym), cnt, solved[sym] ?: 0)
        }
        return Result(formula, list)
    }

    // ============ 私有实现 ============

    private fun nameOf(symbol: String): String =
        ValenceData.elements.firstOrNull { it.symbol == symbol }?.name ?: "?"

    private fun isKnown(symbol: String): Boolean =
        ValenceData.elements.any { it.symbol == symbol }

    /** 解析化学式：H2SO4 → {H:2, S:1, O:4} */
    private fun parse(s: String): Map<String, Int>? {
        val result = linkedMapOf<String, Int>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c.isWhitespace()) { i++; continue }
            if (!c.isUpperCase()) return null
            var j = i + 1
            while (j < s.length && s[j].isLowerCase()) j++
            val sym = s.substring(i, j)
            if (!isKnown(sym)) return null
            i = j
            val numStart = i
            while (i < s.length && s[i].isDigit()) i++
            val count = if (i > numStart) {
                s.substring(numStart, i).toIntOrNull() ?: return null
            } else 1
            result[sym] = (result[sym] ?: 0) + count
        }
        return result
    }

    private val ALKALI = setOf("Li", "Na", "K", "Rb", "Cs")
    private val ALKALINE = setOf("Be", "Mg", "Ca", "Sr", "Ba")
    private val FIXED_METAL = mapOf("Al" to 3, "Zn" to 2, "Ag" to 1)

    private fun solve(parsed: Map<String, Int>): Map<String, Int>? {
        val fixed = mutableMapOf<String, Int>()

        if ("H" in parsed) fixed["H"] = 1
        for (e in parsed.keys) {
            when {
                e in ALKALI -> fixed[e] = 1
                e in ALKALINE -> fixed[e] = 2
                e in FIXED_METAL -> fixed[e] = FIXED_METAL[e]!!
            }
        }

        val unknown = parsed.keys.filter { it !in fixed }

        if (unknown.isEmpty()) {
            val sum = parsed.entries.sumOf { (e, n) -> n * (fixed[e] ?: 0) }
            return if (sum == 0) fixed else null
        }
        if (unknown.size == 1) return solveOne(parsed, fixed, unknown[0])

        // 先固定 O = -2
        if ("O" in unknown) {
            fixed["O"] = -2
            val rest = unknown.filter { it != "O" }
            if (rest.size == 1) return solveOne(parsed, fixed, rest[0])
            return tryCombos(parsed, fixed, rest)
        }

        return tryCombos(parsed, fixed, unknown)
    }

    private fun solveOne(
        parsed: Map<String, Int>,
        fixed: MutableMap<String, Int>,
        target: String
    ): Map<String, Int>? {
        val n = parsed[target] ?: return null
        if (n == 0) return null
        val sumOthers = parsed.entries
            .filter { it.key != target }
            .sumOf { (e, k) -> k * (fixed[e] ?: 0) }
        if (sumOthers % n != 0) return null
        fixed[target] = -sumOthers / n
        return fixed
    }

    private fun tryCombos(
        parsed: Map<String, Int>,
        fixed: Map<String, Int>,
        unknown: List<String>
    ): Map<String, Int>? {
        val candidates = unknown.map { e ->
            ValenceData.elements.firstOrNull { it.symbol == e }?.valences ?: return null
        }
        val current = fixed.toMutableMap()
        var solution: Map<String, Int>? = null

        fun dfs(i: Int): Boolean {
            if (i == unknown.size) {
                val sum = parsed.entries.sumOf { (e, n) -> n * (current[e] ?: 0) }
                if (sum == 0) { solution = current.toMap(); return true }
                return false
            }
            val e = unknown[i]
            for (v in candidates[i]) {
                current[e] = v
                if (dfs(i + 1)) return true
            }
            current.remove(e)
            return false
        }
        dfs(0)
        return solution
    }
}