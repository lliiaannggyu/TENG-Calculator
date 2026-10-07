package com.tengwear.jisuanqi.ui

/**
 * 显示符号常量 — 全局共享
 *
 * Unicode 用转义写法，复制粘贴时不会被破坏。
 */
internal object Symbols {
    const val MINUS   = "\u2212"            // −
    const val TIMES   = "\u00D7"            // ×
    const val DIVIDE  = "\u00F7"            // ÷
    const val PLUSMIN = "\u00B1"            // ±
    const val SQRT    = "\u221A"            // √
    const val SUP_M1  = "\u207B\u00B9"      // ⁻¹
    const val SUP_Y   = "\u02B8"            // ʸ
}

/**
 * 把 ViewModel 里的 ASCII 运算符替换为显示符号
 *
 * 内部表达式："5 - 3 =" → 显示："5 − 3 ="
 */
internal fun prettyExpression(expr: String): String {
    return expr
        .replace("pow", "^")
        .replace("*", Symbols.TIMES)
        .replace("/", Symbols.DIVIDE)
        .replace("-", Symbols.MINUS)
}