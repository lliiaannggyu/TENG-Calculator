package com.tengwear.jisuanqi.logic

/**
 * 模板专用的变量提取 + 公式校验。
 *
 * 复用已有的 [ExpressionEvaluator]：
 *  - 变量统一为单字母（a-z），String 形式
 *  - 常量 pi / e 不算变量（注意：e 不能当变量用）
 *  - 字母与字母之间必须显式写 *，例如 a*b，不能写 ab
 */
object TemplateVars {

    private val FUNCTIONS = setOf(
        "sin", "cos", "tan", "asin", "acos", "atan",
        "ln", "log", "sqrt", "abs"
    )

    private val CONSTANTS = setOf("pi", "e")

    /**
     * 提取公式中用到的单字母变量（小写、去重、按字母序）。
     *
     * 返回 null 表示公式含非法标识符
     * （例如连续字母 `ab`、未知函数名、长度 > 1 的奇怪标识符）。
     */
    fun extract(expression: String): List<String>? {
        val normalized = ExpressionEvaluator.normalize(expression)
        if (normalized.isEmpty()) return emptyList()

        val result = sortedSetOf<String>()
        var i = 0
        while (i < normalized.length) {
            val c = normalized[i]
            if (c.isLetter()) {
                val start = i
                while (i < normalized.length && normalized[i].isLetterOrDigit()) i++
                val name = normalized.substring(start, i).lowercase()

                var j = i
                while (j < normalized.length && normalized[j].isWhitespace()) j++
                val followedByParen = j < normalized.length && normalized[j] == '('

                when {
                    followedByParen && name in FUNCTIONS -> { /* 函数，跳过 */ }
                    name in CONSTANTS -> { /* 常量，跳过 */ }
                    name.length == 1 && name[0] in 'a'..'z' -> result.add(name)
                    else -> return null
                }
            } else {
                i++
            }
        }
        return result.toList()
    }

    /**
     * 校验公式是否可被解析。
     *
     * 返回 null 表示通过，否则返回错误提示。
     */
    fun validate(expression: String): String? {
        val vars = extract(expression)
            ?: return "公式含无效标识符：字母变量之间请用 * 分隔（如 a*b）"
        if (vars.isEmpty()) {
            return "公式至少需要一个字母变量（a-z）"
        }
        // 用不常见的占位值填充所有变量做语法校验，
        // 尽量避开 1/(x-1) 这类在 x=1 时无意义的巧合。
        val probe = vars.associateWith { 3.1416 }
        val r = ExpressionEvaluator.evaluateVars(expression, probe)
        if (r == null) {
            return "公式语法错误，请检查运算符 / 括号 / 函数名"
        }
        return null
    }
}