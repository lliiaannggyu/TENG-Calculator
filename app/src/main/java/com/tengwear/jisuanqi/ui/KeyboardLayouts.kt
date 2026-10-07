package com.tengwear.jisuanqi.ui

/**
 * 所有键盘布局数据
 */

// ============ 计算器键盘（2 页） ============

/**
 * 第 1 页 · 基础
 */
val StandardKeyboard: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("AC",  "A/C"),
        KeyDef("+/-", Symbols.PLUSMIN),
        KeyDef("/",   Symbols.DIVIDE),
        KeyDef("*",   Symbols.TIMES),
        KeyDef("-",   Symbols.MINUS),
        KeyDef("+",   "+")
    ),
    listOf(
        KeyDef("7", "7"), KeyDef("8", "8"), KeyDef("9", "9"),
        KeyDef(".", "."), KeyDef("=", "=")
    ),
    listOf(
        KeyDef("4", "4"), KeyDef("5", "5"), KeyDef("6", "6"),
        KeyDef("0", "0")
    ),
    listOf(
        KeyDef("1", "1"), KeyDef("2", "2"), KeyDef("3", "3")
    )
)

/**
 * 第 2 页 · 精简科学
 *
 * 逗号已去掉
 * ⌫ 已替换为 n 次方根 ⁿ√（用户自己填 n）
 *
 * 用法：输入 a → 按 ⁿ√ → 输入 n → 按 = → 得到 a^(1/n)
 *   例：27 ⁿ√ 3 = 3
 *       32 ⁿ√ 5 = 2
 */
val ScientificKeyboard: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("(",    "("),
        KeyDef(")",    ")"),
        KeyDef("pow",  "x" + Symbols.SUP_Y),
        KeyDef("%",    "%"),
        KeyDef("Ans",  "Ans"),
        KeyDef("root", "\u207F\u221A")   // ⁿ√
    ),
    listOf(
        KeyDef("7", "7"), KeyDef("8", "8"), KeyDef("9", "9"),
        KeyDef(".", "."), KeyDef("=", "=")
    ),
    listOf(
        KeyDef("4", "4"), KeyDef("5", "5"), KeyDef("6", "6"),
        KeyDef("0", "0")
    ),
    listOf(
        KeyDef("1", "1"), KeyDef("2", "2"), KeyDef("3", "3")
    )
)

// ============ 绘图键盘 ============

val PlotKeyboardBasic: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("7", "7"),
        KeyDef("8", "8"),
        KeyDef("9", "9"),
        KeyDef("(", "("),
        KeyDef(")", ")"),
        KeyDef("\u232B", "\u232B")
    ),
    listOf(
        KeyDef("4", "4"),
        KeyDef("5", "5"),
        KeyDef("6", "6"),
        KeyDef("*", "*"),
        KeyDef("/", "/")
    ),
    listOf(
        KeyDef("1", "1"),
        KeyDef("2", "2"),
        KeyDef("3", "3"),
        KeyDef("-", "-"),
        KeyDef("+", "+")
    ),
    listOf(
        KeyDef("0", "0"),
        KeyDef(".", "."),
        KeyDef("x", "x"),
        KeyDef("APPLY", "\u2713")
    )
)

val PlotKeyboardFunc: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("sin(",  "sin"),
        KeyDef("cos(",  "cos"),
        KeyDef("tan(",  "tan"),
        KeyDef("asin(", "sin" + Symbols.SUP_M1),
        KeyDef("acos(", "cos" + Symbols.SUP_M1),
        KeyDef("atan(", "tan" + Symbols.SUP_M1)
    ),
    listOf(
        KeyDef("ln(",   "ln"),
        KeyDef("log(",  "log"),
        KeyDef("sqrt(", Symbols.SQRT),
        KeyDef("abs(",  "abs"),
        KeyDef("^",     "^")
    ),
    listOf(
        KeyDef("pi",   "\u03C0"),
        KeyDef("e",    "e"),
        KeyDef("x",    "x"),
        KeyDef("\u232B", "\u232B")
    ),
    listOf(
        KeyDef("(", "("),
        KeyDef(")", ")"),
        KeyDef("APPLY", "\u2713")
    )
)

// ============ 方程求解键盘 ============

val EquationKeyboardBasic: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("7", "7"),
        KeyDef("8", "8"),
        KeyDef("9", "9"),
        KeyDef("(", "("),
        KeyDef(")", ")"),
        KeyDef("\u232B", "\u232B")
    ),
    listOf(
        KeyDef("4", "4"),
        KeyDef("5", "5"),
        KeyDef("6", "6"),
        KeyDef("*", "*"),
        KeyDef("/", "/"),
        KeyDef("=", "=")
    ),
    listOf(
        KeyDef("1", "1"),
        KeyDef("2", "2"),
        KeyDef("3", "3"),
        KeyDef("-", "-"),
        KeyDef("+", "+"),
        KeyDef("x", "x")
    ),
    listOf(
        KeyDef("0", "0"),
        KeyDef(".", "."),
        KeyDef("^", "^"),
        KeyDef("APPLY", "\u2713")
    )
)

val EquationKeyboardFunc: List<List<KeyDef>> = PlotKeyboardFunc

// ============ 方程组求解键盘 ============

val SystemKeyboardBasic: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("7", "7"),
        KeyDef("8", "8"),
        KeyDef("9", "9"),
        KeyDef("\u232B", "\u232B"),
        KeyDef("APPLY", "\u2713")
    ),
    listOf(
        KeyDef("4", "4"),
        KeyDef("5", "5"),
        KeyDef("6", "6"),
        KeyDef("*", "*"),
        KeyDef("/", "/")
    ),
    listOf(
        KeyDef("1", "1"),
        KeyDef("2", "2"),
        KeyDef("3", "3"),
        KeyDef("-", "-"),
        KeyDef("+", "+")
    ),
    listOf(
        KeyDef("0", "0"),
        KeyDef(".", "."),
        KeyDef("x", "x"),
        KeyDef("y", "y"),
        KeyDef("=", "=")
    )
)

val SystemKeyboardFunc: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("sin(",  "sin"),
        KeyDef("cos(",  "cos"),
        KeyDef("tan(",  "tan"),
        KeyDef("asin(", "sin" + Symbols.SUP_M1),
        KeyDef("acos(", "cos" + Symbols.SUP_M1)
    ),
    listOf(
        KeyDef("atan(", "tan" + Symbols.SUP_M1),
        KeyDef("ln(",   "ln"),
        KeyDef("log(",  "log"),
        KeyDef("sqrt(", Symbols.SQRT),
        KeyDef("abs(",  "abs")
    ),
    listOf(
        KeyDef("^",  "^"),
        KeyDef("pi", "\u03C0"),
        KeyDef("e",  "e"),
        KeyDef(",",  ","),
        KeyDef("z",  "z")
    ),
    listOf(
        KeyDef("(", "("),
        KeyDef(")", ")"),
        KeyDef("APPLY", "\u2713")
    )
)

// ============ 行宽 ============

val CalcRowWidths: List<Float> = listOf(1.00f, 0.96f, 0.82f, 0.62f)

val PlotRowWidthsBasic: List<Float> = listOf(1.00f, 0.96f, 0.92f, 0.80f)

val PlotRowWidthsFunc: List<Float> = listOf(1.00f, 0.96f, 0.82f, 0.62f)

val EquationRowWidthsBasic: List<Float> = listOf(1.00f, 1.00f, 1.00f, 0.72f)

val EquationRowWidthsFunc: List<Float> = PlotRowWidthsFunc

val SystemRowWidthsBasic: List<Float> = listOf(1.00f, 1.00f, 1.00f, 1.00f)

val SystemRowWidthsFunc: List<Float> = listOf(1.00f, 1.00f, 1.00f, 0.60f)

// ============ 不等式求解键盘 ============

val InequalityKeyboard: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("7", "7"),
        KeyDef("8", "8"),
        KeyDef("9", "9"),
        KeyDef("(", "("),
        KeyDef(")", ")"),
        KeyDef("\u232B", "\u232B")
    ),
    listOf(
        KeyDef("4", "4"),
        KeyDef("5", "5"),
        KeyDef("6", "6"),
        KeyDef("*", "*"),
        KeyDef("/", "/"),
        KeyDef("<", "<")
    ),
    listOf(
        KeyDef("1", "1"),
        KeyDef("2", "2"),
        KeyDef("3", "3"),
        KeyDef("-", "-"),
        KeyDef("+", "+"),
        KeyDef(">", ">")
    ),
    listOf(
        KeyDef("0", "0"),
        KeyDef(".", "."),
        KeyDef("x", "x"),
        KeyDef("\u2264", "\u2264"),
        KeyDef("\u2265", "\u2265"),
        KeyDef("APPLY", "\u2713")
    )
)

val InequalityRowWidths: List<Float> = listOf(1.00f, 1.00f, 1.00f, 1.00f)

// ============ 线性回归键盘 ============

val RegressionKeyboard: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("7", "7"),
        KeyDef("8", "8"),
        KeyDef("9", "9"),
        KeyDef(".", "."),
        KeyDef(",", ","),
        KeyDef("\u232B", "\u232B")
    ),
    listOf(
        KeyDef("4", "4"),
        KeyDef("5", "5"),
        KeyDef("6", "6"),
        KeyDef("-", "-"),
        KeyDef("+", "+"),
        KeyDef("APPLY", "\u2713")
    ),
    listOf(
        KeyDef("1", "1"),
        KeyDef("2", "2"),
        KeyDef("3", "3"),
        KeyDef("0", "0")
    )
)

val RegressionRowWidths: List<Float> = listOf(1.00f, 1.00f, 0.72f)

// ============ 科学计数法键盘 ============

val ScientificNotationKeyboard: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("7", "7"),
        KeyDef("8", "8"),
        KeyDef("9", "9"),
        KeyDef(".", "."),
        KeyDef("E", "E"),
        KeyDef("\u232B", "\u232B")
    ),
    listOf(
        KeyDef("4", "4"),
        KeyDef("5", "5"),
        KeyDef("6", "6"),
        KeyDef("-", "-"),
        KeyDef("+", "+"),
        KeyDef("APPLY", "\u2713")
    ),
    listOf(
        KeyDef("1", "1"),
        KeyDef("2", "2"),
        KeyDef("3", "3"),
        KeyDef("0", "0")
    )
)

val ScientificNotationRowWidths: List<Float> = listOf(1.00f, 1.00f, 0.72f)

// ============ 数字与汉字键盘 ============

val NumberChineseKeyboard: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("7", "7"),
        KeyDef("8", "8"),
        KeyDef("9", "9"),
        KeyDef(".", "."),
        KeyDef("\u232B", "\u232B")
    ),
    listOf(
        KeyDef("4", "4"),
        KeyDef("5", "5"),
        KeyDef("6", "6"),
        KeyDef("-", "-"),
        KeyDef("APPLY", "\u2713")
    ),
    listOf(
        KeyDef("1", "1"),
        KeyDef("2", "2"),
        KeyDef("3", "3"),
        KeyDef("0", "0")
    )
)

val NumberChineseRowWidths: List<Float> = listOf(1.00f, 1.00f, 0.78f)

// ============ 进制转换键盘 ============

val NumberBaseKeyboard: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("1", "1"),
        KeyDef("2", "2"),
        KeyDef("3", "3"),
        KeyDef("4", "4"),
        KeyDef("5", "5"),
        KeyDef("\u232B", "\u232B")
    ),
    listOf(
        KeyDef("6", "6"),
        KeyDef("7", "7"),
        KeyDef("8", "8"),
        KeyDef("9", "9"),
        KeyDef("0", "0"),
        KeyDef("APPLY", "\u2713")
    ),
    listOf(
        KeyDef("A", "A"),
        KeyDef("B", "B"),
        KeyDef("C", "C"),
        KeyDef("D", "D"),
        KeyDef("E", "E"),
        KeyDef("F", "F")
    )
)

val NumberBaseRowWidths: List<Float> = listOf(1.00f, 1.00f, 1.00f)

// ============ 化学配平键盘 ============

val ChemKeyboardNumbers: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("1", "1"), KeyDef("2", "2"), KeyDef("3", "3"),
        KeyDef("4", "4"), KeyDef("5", "5"), KeyDef("\u232B", "\u232B")
    ),
    listOf(
        KeyDef("6", "6"), KeyDef("7", "7"), KeyDef("8", "8"),
        KeyDef("9", "9"), KeyDef("0", "0"), KeyDef("APPLY", "\u2713")
    ),
    listOf(
        KeyDef("+", "+"),
        KeyDef("=", "="),
        KeyDef("(", "("),
        KeyDef(")", ")"),
        KeyDef("\u00B7", "\u00B7"),
        KeyDef("CLEAR", "C")
    )
)

val ChemRowWidthsNumbers: List<Float> = listOf(1.00f, 1.00f, 1.00f)

val ChemKeyboardLetters: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("A", "A"), KeyDef("B", "B"), KeyDef("C", "C"),
        KeyDef("D", "D"), KeyDef("E", "E"), KeyDef("F", "F")
    ),
    listOf(
        KeyDef("G", "G"), KeyDef("H", "H"), KeyDef("I", "I"),
        KeyDef("K", "K"), KeyDef("L", "L"), KeyDef("M", "M")
    ),
    listOf(
        KeyDef("N", "N"), KeyDef("O", "O"), KeyDef("P", "P"),
        KeyDef("S", "S"), KeyDef("T", "T"), KeyDef("U", "U")
    ),
    listOf(
        KeyDef("V", "V"), KeyDef("W", "W"), KeyDef("X", "X"),
        KeyDef("Y", "Y"), KeyDef("Z", "Z"), KeyDef("\u232B", "\u232B")
    )
)

val ChemRowWidthsLetters: List<Float> = listOf(1.00f, 1.00f, 1.00f, 1.00f)

val ChemKeyboardLower: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("a", "a"), KeyDef("b", "b"), KeyDef("c", "c"),
        KeyDef("d", "d"), KeyDef("e", "e"), KeyDef("f", "f")
    ),
    listOf(
        KeyDef("g", "g"), KeyDef("h", "h"), KeyDef("i", "i"),
        KeyDef("k", "k"), KeyDef("l", "l"), KeyDef("m", "m")
    ),
    listOf(
        KeyDef("n", "n"), KeyDef("o", "o"), KeyDef("p", "p"),
        KeyDef("r", "r"), KeyDef("s", "s"), KeyDef("t", "t")
    ),
    listOf(
        KeyDef("u", "u"), KeyDef("v", "v"), KeyDef("w", "w"),
        KeyDef("x", "x"), KeyDef("y", "y"), KeyDef("APPLY", "\u2713")
    )
)

val ChemRowWidthsLower: List<Float> = listOf(1.00f, 1.00f, 1.00f, 1.00f)