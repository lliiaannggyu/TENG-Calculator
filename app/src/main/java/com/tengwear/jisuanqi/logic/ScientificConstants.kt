package com.tengwear.jisuanqi.logic

import kotlin.math.PI
import kotlin.math.E

/**
 * 科学计算器的物理 / 数学常量
 *
 * 显示符号用 ASCII 或 Unicode 上标，键值用 key 字段。
 */
data class SciConstant(
    val key: String,      // 键盘 key
    val label: String,    // 按键显示文本
    val value: Double,    // 数值
    val name: String      // 完整名称（用于历史记录 / 提示）
)

object ScientificConstants {

    val all: List<SciConstant> = listOf(
        // ===== 第 4 页 第 1 行 =====
        SciConstant("const_c",    "c",    299792458.0,       "光速"),
        SciConstant("const_h",    "h",    6.62607015e-34,    "普朗克常数"),
        SciConstant("const_k",    "k",    1.380649e-23,      "玻尔兹曼常数"),
        SciConstant("const_Na",   "N\u2090", 6.02214076e23,  "阿伏伽德罗常数"),
        SciConstant("const_R",    "R",    8.314462618,       "气体常数"),
        SciConstant("const_G",    "G",    6.67430e-11,       "万有引力常数"),

        // ===== 第 4 页 第 2 行 =====
        SciConstant("const_g",    "g",    9.80665,           "重力加速度"),
        SciConstant("const_eps0", "\u03B5\u2080", 8.8541878128e-12, "真空介电常数"),
        SciConstant("const_mu0",  "\u03BC\u2080", 1.25663706212e-6, "真空磁导率"),
        SciConstant("const_me",   "m\u2091", 9.1093837015e-31, "电子质量"),
        SciConstant("const_mp",   "m\u209A", 1.67262192369e-27, "质子质量"),
        SciConstant("const_Me",   "M\u2091", 5.9722e24,       "地球质量"),

        // ===== 第 4 页 第 3 行 =====
        SciConstant("const_phi",  "\u03C6", 1.618033988749895, "黄金比例"),
        SciConstant("const_gam",  "\u03B3", 0.5772156649015329, "欧拉-马歇罗尼常数"),
        SciConstant("const_tau",  "\u03C4", 2.0 * PI,          "圆周常数 2\u03C0"),
        SciConstant("const_ln2",  "ln2",   0.6931471805599453, "2 的自然对数"),
        SciConstant("const_ln10", "ln10",  2.302585092994046,  "10 的自然对数"),
        SciConstant("const_log2e", "log\u2082e", 1.4426950408889634, "log\u2082(e)")
    )

    private val byKey: Map<String, SciConstant> = all.associateBy { it.key }

    fun find(key: String): SciConstant? = byKey[key]

    fun isConstantKey(key: String): Boolean = key.startsWith("const_")

    /** 数学常量 e 和 π 不算在常量页，走各自单独的 key */
    val PI_VALUE: Double = PI
    val E_VALUE: Double = E
}