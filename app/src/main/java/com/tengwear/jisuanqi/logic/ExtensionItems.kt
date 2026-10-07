package com.tengwear.jisuanqi.ui

internal object ExtensionItems {

    const val HISTORY = "history"
    const val NOTES = "notes"
    const val TEMPLATES = "templates"
    const val PLOT = "plot"
    const val EQUATION = "equation"
    const val SYSTEM = "system"
    const val INEQUALITY = "inequality"
    const val INEQUALITY_SYSTEM = "inequality_system"
    const val MONOTONICITY = "monotonicity"
    const val DERIVATIVE = "derivative"
    const val INTEGRAL = "integral"
    const val REGRESSION = "regression"
    const val SCIENTIFIC = "scientific"
    const val NUMBER_CHINESE = "number_chinese"
    const val NUMBER_BASE = "number_base"
    const val UNIT_CONVERTER = "unit_converter"
    const val CHEMISTRY = "chemistry"
    const val PERIODIC_TABLE = "periodic_table"
    const val VALENCE = "valence"
    const val SCIENTIFIC_CALC = "scientific_calc"

    val defaultOrder: List<String> = listOf(
        HISTORY, NOTES, TEMPLATES, PLOT, EQUATION, SYSTEM,
        INEQUALITY, INEQUALITY_SYSTEM,
        MONOTONICITY, DERIVATIVE, INTEGRAL,
        REGRESSION, SCIENTIFIC,
        SCIENTIFIC_CALC,
        NUMBER_CHINESE, NUMBER_BASE, UNIT_CONVERTER,
        CHEMISTRY, PERIODIC_TABLE, VALENCE
    )

    private val titleMap: Map<String, String> = mapOf(
        HISTORY to "历史记录",
        NOTES to "笔记",
        TEMPLATES to "公式模板",
        PLOT to "函数绘图",
        EQUATION to "方程求解",
        SYSTEM to "方程组求解",
        INEQUALITY to "不等式求解",
        INEQUALITY_SYSTEM to "不等式组",
        MONOTONICITY to "单调性分析",
        DERIVATIVE to "函数求导",
        INTEGRAL to "定积分",
        REGRESSION to "线性回归",
        SCIENTIFIC to "科学计数法",
        SCIENTIFIC_CALC to "科学计算",
        NUMBER_CHINESE to "数字与汉字",
        NUMBER_BASE to "进制转换",
        UNIT_CONVERTER to "单位换算",
        CHEMISTRY to "化学配平",
        PERIODIC_TABLE to "元素周期表",
        VALENCE to "元素化合价"
    )

    private val subtitleMap: Map<String, String> = mapOf(
        HISTORY to "查看 / 插入历史运算",
        NOTES to "随手记一笔，自动保存",
        TEMPLATES to "自建公式，输入变量即算",
        PLOT to "绘制 y = f(x) 曲线",
        EQUATION to "输入方程求根",
        SYSTEM to "二元 / 三元一次方程组",
        INEQUALITY to "一元一次 / 二次不等式",
        INEQUALITY_SYSTEM to "多个不等式求交集",
        MONOTONICITY to "求单调区间与极值点",
        DERIVATIVE to "求 f′(x) 表达式",
        INTEGRAL to "求 ∫[a,b] f(x)dx",
        REGRESSION to "最小二乘拟合 y = ax + b",
        SCIENTIFIC to "普通 ↔ a×10ⁿ 互转",
        SCIENTIFIC_CALC to "三角 / 对数 / 幂 / 常量 / 内存",
        NUMBER_CHINESE to "12345 → 一万二千三百四十五",
        NUMBER_BASE to "二 / 八 / 十 / 十六互转",
        UNIT_CONVERTER to "长度 / 重量 / 温度 / 面积 / 体积",
        CHEMISTRY to "配平化学反应方程式",
        PERIODIC_TABLE to "查阅 118 种元素信息",
        VALENCE to "常见元素的正负价态"
    )

    fun titleOf(id: String): String = titleMap[id] ?: id

    fun subtitleOf(id: String): String = subtitleMap[id] ?: ""

    fun resolveOrder(custom: List<String>): List<String> {
        val result = mutableListOf<String>()
        for (id in custom) {
            if (id in defaultOrder && id !in result) result.add(id)
        }
        for (id in defaultOrder) {
            if (id !in result) result.add(id)
        }
        return result
    }
}