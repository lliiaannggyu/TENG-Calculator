package com.tengwear.jisuanqi.logic

data class InequalitySystemResult(
    val solution: String,
    val interval: String,
    val steps: List<String>,
    val error: String?
)

object InequalitySystemSolver {

    fun solve(inequalities: List<String>): InequalitySystemResult {
        val valid = inequalities.filter { it.isNotBlank() }
        if (valid.isEmpty()) {
            return InequalitySystemResult("", "", emptyList(), "未添加不等式")
        }

        val steps = ArrayList<String>()
        steps.add("共 ${valid.size} 个不等式")
        steps.add("")

        var combined = IntervalSet.all()

        for ((idx, line) in valid.withIndex()) {
            val parsed = parseInequality(line)
            if (parsed == null) {
                return InequalitySystemResult(
                    "", "", steps,
                    "第 ${idx + 1} 个不等式格式错误"
                )
            }
            val (lhs, op, rhs) = parsed
            val set = InequalitySolver.solveInequalityAsSet(lhs, op, rhs)

            steps.add("${idx + 1}. ${prettyDisplay(line)}")
            steps.add("   解集：${set.toSymbolic()}")

            combined = combined.intersect(set)

            if (combined.isEmpty()) {
                steps.add("")
                steps.add("与前面结果交集为空 → 无解")
                return InequalitySystemResult("无解", "\u2205", steps, null)
            }
        }

        steps.add("")
        steps.add("取所有解集的交集：")

        val human = combined.toHumanReadable()
        val symbolic = combined.toSymbolic()

        steps.add("  $symbolic")

        return InequalitySystemResult(
            solution = human,
            interval = symbolic,
            steps = steps,
            error = null
        )
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

    private fun prettyDisplay(s: String): String =
        s.replace(">=", "\u2265").replace("<=", "\u2264")
}