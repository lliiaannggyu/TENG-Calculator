package com.tengwear.jisuanqi.logic

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * 单个单位定义
 * toBase:   当前单位值 → 基准单位值
 * fromBase: 基准单位值 → 当前单位值
 */
data class UnitDef(
    val symbol: String,
    val name: String,
    val toBase: (Double) -> Double,
    val fromBase: (Double) -> Double
)

data class UnitCategory(
    val name: String,
    val units: List<UnitDef>
)

data class UnitConverterResult(
    val categoryName: String,
    val sourceUnit: UnitDef,
    val inputValue: Double,
    val conversions: List<Pair<UnitDef, Double>>,
    val error: String?
)

object UnitConverterSolver {

    val categories: List<UnitCategory> = listOf(

        // ============ 长度 ============
        UnitCategory(
            name = "长度",
            units = listOf(
                UnitDef("nm",  "纳米",   { it * 1e-9 },       { it / 1e-9 }),
                UnitDef("\u00B5m", "微米", { it * 1e-6 },     { it / 1e-6 }),
                UnitDef("mm",  "毫米",   { it * 1e-3 },       { it / 1e-3 }),
                UnitDef("cm",  "厘米",   { it * 1e-2 },       { it / 1e-2 }),
                UnitDef("dm",  "分米",   { it * 1e-1 },       { it / 1e-1 }),
                UnitDef("m",   "米",     { it },              { it }),
                UnitDef("dam", "十米",   { it * 10.0 },       { it / 10.0 }),
                UnitDef("hm",  "百米",   { it * 100.0 },      { it / 100.0 }),
                UnitDef("km",  "千米",   { it * 1000.0 },     { it / 1000.0 }),
                UnitDef("in",  "英寸",   { it * 0.0254 },     { it / 0.0254 }),
                UnitDef("ft",  "英尺",   { it * 0.3048 },     { it / 0.3048 }),
                UnitDef("yd",  "码",     { it * 0.9144 },     { it / 0.9144 }),
                UnitDef("mi",  "英里",   { it * 1609.344 },   { it / 1609.344 }),
                UnitDef("nmi", "海里",   { it * 1852.0 },     { it / 1852.0 }),
                UnitDef("fur", "浪",     { it * 201.168 },    { it / 201.168 }),
                UnitDef("ch",  "链",     { it * 20.1168 },    { it / 20.1168 }),
                UnitDef("fath","英寻",   { it * 1.8288 },     { it / 1.8288 }),
                UnitDef("mil", "密尔",   { it * 2.54e-5 },    { it / 2.54e-5 }),
                UnitDef("AU",  "天文单位", { it * 1.495978707e11 }, { it / 1.495978707e11 }),
                UnitDef("ly",  "光年",   { it * 9.4607304725808e15 }, { it / 9.4607304725808e15 }),
                UnitDef("pc",  "秒差距", { it * 3.0856775814914e16 }, { it / 3.0856775814914e16 }),
                UnitDef("寸",   "市寸",   { it * 0.03333 },    { it / 0.03333 }),
                UnitDef("尺",   "市尺",   { it * 0.3333 },     { it / 0.3333 }),
                UnitDef("丈",   "市丈",   { it * 3.3333 },     { it / 3.3333 }),
                UnitDef("里",   "市里",   { it * 500.0 },      { it / 500.0 })
            )
        ),

        // ============ 重量/质量 ============
        UnitCategory(
            name = "重量",
            units = listOf(
                UnitDef("mg",  "毫克",   { it * 1e-6 },        { it / 1e-6 }),
                UnitDef("g",   "克",     { it * 1e-3 },        { it / 1e-3 }),
                UnitDef("dag", "十克",   { it * 1e-2 },        { it / 1e-2 }),
                UnitDef("hg",  "百克",   { it * 1e-1 },        { it / 1e-1 }),
                UnitDef("kg",  "千克",   { it },               { it }),
                UnitDef("t",   "吨",     { it * 1000.0 },      { it / 1000.0 }),
                UnitDef("kt",  "千吨",   { it * 1e6 },         { it / 1e6 }),
                UnitDef("gr",  "格令",   { it * 6.479891e-5 }, { it / 6.479891e-5 }),
                UnitDef("ct",  "克拉",   { it * 2e-4 },        { it / 2e-4 }),
                UnitDef("oz",  "盎司",   { it * 0.028349523125 }, { it / 0.028349523125 }),
                UnitDef("lb",  "磅",     { it * 0.45359237 },  { it / 0.45359237 }),
                UnitDef("st",  "英石",   { it * 6.35029318 },  { it / 6.35029318 }),
                UnitDef("ton_us", "美吨", { it * 907.18474 },  { it / 907.18474 }),
                UnitDef("ton_uk", "英吨", { it * 1016.0469088 }, { it / 1016.0469088 }),
                UnitDef("担",   "市担",   { it * 50.0 },        { it / 50.0 }),
                UnitDef("斤",   "市斤",   { it * 0.5 },         { it / 0.5 }),
                UnitDef("两",   "市两",   { it * 0.05 },        { it / 0.05 }),
                UnitDef("钱",   "市钱",   { it * 0.005 },       { it / 0.005 })
            )
        ),

        // ============ 温度 ============
        UnitCategory(
            name = "温度",
            units = listOf(
                UnitDef("\u00B0C", "摄氏度", { it },                       { it }),
                UnitDef("\u00B0F", "华氏度", { (it - 32) * 5.0 / 9.0 },    { it * 9.0 / 5.0 + 32 }),
                UnitDef("K",       "开尔文", { it - 273.15 },              { it + 273.15 }),
                UnitDef("\u00B0R", "兰氏度", { (it - 491.67) * 5.0 / 9.0 },{ it * 9.0 / 5.0 + 491.67 }),
                UnitDef("\u00B0Re", "列氏度", { it * 1.25 },               { it * 0.8 })
            )
        ),

        // ============ 面积 ============
        UnitCategory(
            name = "面积",
            units = listOf(
                UnitDef("mm\u00B2", "平方毫米", { it * 1e-6 },       { it / 1e-6 }),
                UnitDef("cm\u00B2", "平方厘米", { it * 1e-4 },       { it / 1e-4 }),
                UnitDef("dm\u00B2", "平方分米", { it * 1e-2 },       { it / 1e-2 }),
                UnitDef("m\u00B2",  "平方米",   { it },              { it }),
                UnitDef("dam\u00B2","平方十米", { it * 100.0 },      { it / 100.0 }),
                UnitDef("hm\u00B2", "平方百米", { it * 10000.0 },    { it / 10000.0 }),
                UnitDef("km\u00B2", "平方千米", { it * 1e6 },        { it / 1e6 }),
                UnitDef("ha",   "公顷",     { it * 10000.0 },    { it / 10000.0 }),
                UnitDef("are",  "公亩",     { it * 100.0 },      { it / 100.0 }),
                UnitDef("in\u00B2", "平方英寸", { it * 0.00064516 },{ it / 0.00064516 }),
                UnitDef("ft\u00B2", "平方英尺", { it * 0.09290304 },{ it / 0.09290304 }),
                UnitDef("yd\u00B2", "平方码",   { it * 0.83612736 },{ it / 0.83612736 }),
                UnitDef("mi\u00B2", "平方英里", { it * 2589988.110336 },{ it / 2589988.110336 }),
                UnitDef("acre", "英亩",     { it * 4046.8564224 },{ it / 4046.8564224 }),
                UnitDef("亩",   "亩",       { it * 666.6666667 },{ it / 666.6666667 }),
                UnitDef("顷",   "顷",       { it * 66666.66667 },{ it / 66666.66667 })
            )
        ),

        // ============ 体积 ============
        UnitCategory(
            name = "体积",
            units = listOf(
                UnitDef("mL",   "毫升",     { it * 1e-3 },       { it / 1e-3 }),
                UnitDef("cL",   "厘升",     { it * 1e-2 },       { it / 1e-2 }),
                UnitDef("dL",   "分升",     { it * 1e-1 },       { it / 1e-1 }),
                UnitDef("L",    "升",       { it },              { it }),
                UnitDef("hL",   "百升",     { it * 100.0 },      { it / 100.0 }),
                UnitDef("cm\u00B3", "立方厘米", { it * 1e-3 },   { it / 1e-3 }),
                UnitDef("dm\u00B3", "立方分米", { it },          { it }),
                UnitDef("m\u00B3", "立方米",   { it * 1000.0 }, { it / 1000.0 }),
                UnitDef("in\u00B3", "立方英寸", { it * 0.016387064 }, { it / 0.016387064 }),
                UnitDef("ft\u00B3", "立方英尺", { it * 28.316846592 }, { it / 28.316846592 }),
                UnitDef("floz_us", "液量盎司(美)", { it * 0.0295735295625 }, { it / 0.0295735295625 }),
                UnitDef("cup_us",  "杯(美)",      { it * 0.2365882365 }, { it / 0.2365882365 }),
                UnitDef("pt_us",   "品脱(美)",    { it * 0.473176473 }, { it / 0.473176473 }),
                UnitDef("qt_us",   "夸脱(美)",    { it * 0.946352946 }, { it / 0.946352946 }),
                UnitDef("gal_us",  "加仑(美)",    { it * 3.785411784 }, { it / 3.785411784 }),
                UnitDef("gal_uk",  "加仑(英)",    { it * 4.54609 },     { it / 4.54609 }),
                UnitDef("tbsp",    "汤匙(美)",    { it * 0.0147867648 }, { it / 0.0147867648 }),
                UnitDef("tsp",     "茶匙(美)",    { it * 0.0049289216 }, { it / 0.0049289216 })
            )
        ),

        // ============ 时间 ============
        UnitCategory(
            name = "时间",
            units = listOf(
                UnitDef("ns",   "纳秒",   { it * 1e-9 },      { it / 1e-9 }),
                UnitDef("\u00B5s", "微秒", { it * 1e-6 },     { it / 1e-6 }),
                UnitDef("ms",   "毫秒",   { it * 1e-3 },      { it / 1e-3 }),
                UnitDef("s",    "秒",     { it },             { it }),
                UnitDef("min",  "分钟",   { it * 60.0 },      { it / 60.0 }),
                UnitDef("h",    "小时",   { it * 3600.0 },    { it / 3600.0 }),
                UnitDef("d",    "天",     { it * 86400.0 },   { it / 86400.0 }),
                UnitDef("wk",   "周",     { it * 604800.0 },  { it / 604800.0 }),
                UnitDef("mo",   "月(30天)", { it * 2592000.0 }, { it / 2592000.0 }),
                UnitDef("yr",   "年(365天)", { it * 31536000.0 }, { it / 31536000.0 }),
                UnitDef("十年",  "十年",   { it * 315360000.0 }, { it / 315360000.0 }),
                UnitDef("世纪",  "世纪",   { it * 3153600000.0 }, { it / 3153600000.0 })
            )
        ),

        // ============ 速度 ============
        UnitCategory(
            name = "速度",
            units = listOf(
                UnitDef("m/s",  "米每秒",  { it },               { it }),
                UnitDef("km/h", "千米每小时", { it / 3.6 },      { it * 3.6 }),
                UnitDef("mph",  "英里每小时", { it * 0.44704 },  { it / 0.44704 }),
                UnitDef("ft/s", "英尺每秒",  { it * 0.3048 },    { it / 0.3048 }),
                UnitDef("kn",   "节",      { it * 0.514444 },    { it / 0.514444 }),
                UnitDef("mach", "马赫",    { it * 340.3 },       { it / 340.3 })
            )
        ),

        // ============ 数据存储 ============
        UnitCategory(
            name = "数据存储",
            units = listOf(
                UnitDef("bit",  "比特",   { it },              { it }),
                UnitDef("B",    "字节",   { it * 8.0 },        { it / 8.0 }),
                UnitDef("KB",   "千字节", { it * 8.0 * 1024.0 }, { it / (8.0 * 1024.0) }),
                UnitDef("MB",   "兆字节", { it * 8.0 * 1024.0 * 1024.0 }, { it / (8.0 * 1024.0 * 1024.0) }),
                UnitDef("GB",   "吉字节", { it * 8.0 * 1024.0 * 1024.0 * 1024.0 }, { it / (8.0 * 1024.0 * 1024.0 * 1024.0) }),
                UnitDef("TB",   "太字节", { it * 8.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0 }, { it / (8.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0) }),
                UnitDef("PB",   "拍字节", { it * 8.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0 }, { it / (8.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0) })
            )
        ),

        // ============ 角度 ============
        UnitCategory(
            name = "角度",
            units = listOf(
                UnitDef("\u00B0", "度",   { it },                       { it }),
                UnitDef("rad",    "弧度", { it * 180.0 / Math.PI },     { it * Math.PI / 180.0 }),
                UnitDef("grad",   "梯度", { it * 0.9 },                 { it / 0.9 }),
                UnitDef("\u2032", "角分", { it / 60.0 },                { it * 60.0 }),
                UnitDef("\u2033", "角秒", { it / 3600.0 },              { it * 3600.0 }),
                UnitDef("圈",     "圈",   { it * 360.0 },               { it / 360.0 })
            )
        ),

        // ============ 压力 ============
        UnitCategory(
            name = "压力",
            units = listOf(
                UnitDef("Pa",   "帕",       { it },              { it }),
                UnitDef("kPa",  "千帕",     { it * 1000.0 },     { it / 1000.0 }),
                UnitDef("MPa",  "兆帕",     { it * 1e6 },        { it / 1e6 }),
                UnitDef("bar",  "巴",       { it * 100000.0 },   { it / 100000.0 }),
                UnitDef("atm",  "标准大气压", { it * 101325.0 },  { it / 101325.0 }),
                UnitDef("mmHg", "毫米汞柱",  { it * 133.322 },    { it / 133.322 }),
                UnitDef("psi",  "磅力/平方英寸", { it * 6894.757 }, { it / 6894.757 })
            )
        )
    )

    fun solve(
        categoryIndex: Int,
        sourceUnitIndex: Int,
        inputText: String
    ): UnitConverterResult {
        if (categoryIndex !in categories.indices) {
            return UnitConverterResult("", UnitDef("", "", { it }, { it }), 0.0, emptyList(), "类别无效")
        }
        val category = categories[categoryIndex]
        if (sourceUnitIndex !in category.units.indices) {
            return UnitConverterResult("", UnitDef("", "", { it }, { it }), 0.0, emptyList(), "单位无效")
        }

        val value = inputText.trim().toDoubleOrNull()
            ?: return UnitConverterResult(
                category.name,
                category.units[sourceUnitIndex],
                0.0, emptyList(),
                "请输入有效的数字"
            )

        val sourceUnit = category.units[sourceUnitIndex]
        val baseValue = sourceUnit.toBase(value)
        val conversions = category.units.map { unit ->
            unit to unit.fromBase(baseValue)
        }

        return UnitConverterResult(
            categoryName = category.name,
            sourceUnit = sourceUnit,
            inputValue = value,
            conversions = conversions,
            error = null
        )
    }

    fun formatNum(v: Double): String {
        if (v.isNaN() || v.isInfinite()) return "\u2014"
        if (abs(v) < 1e-15) return "0"

        val absV = abs(v)
        // 极大/极小值用科学计数法
        if (absV >= 1e12 || absV < 1e-6) {
            val exp = Math.floor(Math.log10(absV)).toInt()
            val m = v / Math.pow(10.0, exp.toDouble())
            val mantissa = String.format("%.4f", m).trimEnd('0').trimEnd('.')
            return "$mantissa \u00D7 10^$exp"
        }

        // 整数直接显示
        val r = v.roundToLong()
        if (abs(v - r.toDouble()) < 1e-9) return r.toString()

        // 其他保留 6 位小数
        val s = String.format("%.6f", v)
        return s.trimEnd('0').trimEnd('.')
    }
}