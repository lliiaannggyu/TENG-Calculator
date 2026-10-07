package com.tengwear.jisuanqi.logic

/**
 * 化学元素数据
 */
data class Element(
    val atomicNumber: Int,
    val symbol: String,      // 元素符号（如 H, He, Na）
    val name: String,        // 中文名
    val nameEn: String,      // 英文名
    val atomicMass: Double,  // 相对原子质量
    val category: String     // 类别（中文）
)

object PeriodicElements {

    const val NONMETAL        = "非金属"
    const val NOBLE_GAS       = "稀有气体"
    const val ALKALI          = "碱金属"
    const val ALKALINE        = "碱土金属"
    const val METALLOID       = "类金属"
    const val HALOGEN         = "卤素"
    const val TRANSITION      = "过渡金属"
    const val POST_TRANSITION = "主族金属"
    const val LANTHANIDE      = "镧系"
    const val ACTINIDE        = "锕系"

    private fun e(n: Int, s: String, c: String, en: String, m: Double, cat: String) =
        Element(n, s, c, en, m, cat)

    val all: List<Element> = listOf(
        e(1,   "H",  "氢", "Hydrogen",     1.008,  NONMETAL),
        e(2,   "He", "氦", "Helium",       4.003,  NOBLE_GAS),
        e(3,   "Li", "锂", "Lithium",      6.94,   ALKALI),
        e(4,   "Be", "铍", "Beryllium",    9.012,  ALKALINE),
        e(5,   "B",  "硼", "Boron",        10.81,  METALLOID),
        e(6,   "C",  "碳", "Carbon",       12.011, NONMETAL),
        e(7,   "N",  "氮", "Nitrogen",     14.007, NONMETAL),
        e(8,   "O",  "氧", "Oxygen",       15.999, NONMETAL),
        e(9,   "F",  "氟", "Fluorine",     18.998, HALOGEN),
        e(10,  "Ne", "氖", "Neon",         20.180, NOBLE_GAS),
        e(11,  "Na", "钠", "Sodium",       22.990, ALKALI),
        e(12,  "Mg", "镁", "Magnesium",    24.305, ALKALINE),
        e(13,  "Al", "铝", "Aluminium",    26.982, POST_TRANSITION),
        e(14,  "Si", "硅", "Silicon",      28.085, METALLOID),
        e(15,  "P",  "磷", "Phosphorus",   30.974, NONMETAL),
        e(16,  "S",  "硫", "Sulfur",       32.06,  NONMETAL),
        e(17,  "Cl", "氯", "Chlorine",     35.45,  HALOGEN),
        e(18,  "Ar", "氩", "Argon",        39.948, NOBLE_GAS),
        e(19,  "K",  "钾", "Potassium",    39.098, ALKALI),
        e(20,  "Ca", "钙", "Calcium",      40.078, ALKALINE),
        e(21,  "Sc", "钪", "Scandium",     44.956, TRANSITION),
        e(22,  "Ti", "钛", "Titanium",     47.867, TRANSITION),
        e(23,  "V",  "钒", "Vanadium",     50.942, TRANSITION),
        e(24,  "Cr", "铬", "Chromium",     51.996, TRANSITION),
        e(25,  "Mn", "锰", "Manganese",    54.938, TRANSITION),
        e(26,  "Fe", "铁", "Iron",         55.845, TRANSITION),
        e(27,  "Co", "钴", "Cobalt",       58.933, TRANSITION),
        e(28,  "Ni", "镍", "Nickel",       58.693, TRANSITION),
        e(29,  "Cu", "铜", "Copper",       63.546, TRANSITION),
        e(30,  "Zn", "锌", "Zinc",         65.38,  TRANSITION),
        e(31,  "Ga", "镓", "Gallium",      69.723, POST_TRANSITION),
        e(32,  "Ge", "锗", "Germanium",    72.630, METALLOID),
        e(33,  "As", "砷", "Arsenic",      74.922, METALLOID),
        e(34,  "Se", "硒", "Selenium",     78.971, NONMETAL),
        e(35,  "Br", "溴", "Bromine",      79.904, HALOGEN),
        e(36,  "Kr", "氪", "Krypton",      83.798, NOBLE_GAS),
        e(37,  "Rb", "铷", "Rubidium",     85.468, ALKALI),
        e(38,  "Sr", "锶", "Strontium",    87.62,  ALKALINE),
        e(39,  "Y",  "钇", "Yttrium",      88.906, TRANSITION),
        e(40,  "Zr", "锆", "Zirconium",    91.224, TRANSITION),
        e(41,  "Nb", "铌", "Niobium",      92.906, TRANSITION),
        e(42,  "Mo", "钼", "Molybdenum",   95.95,  TRANSITION),
        e(43,  "Tc", "锝", "Technetium",   98.0,   TRANSITION),
        e(44,  "Ru", "钌", "Ruthenium",    101.07, TRANSITION),
        e(45,  "Rh", "铑", "Rhodium",      102.91, TRANSITION),
        e(46,  "Pd", "钯", "Palladium",    106.42, TRANSITION),
        e(47,  "Ag", "银", "Silver",       107.87, TRANSITION),
        e(48,  "Cd", "镉", "Cadmium",      112.41, TRANSITION),
        e(49,  "In", "铟", "Indium",       114.82, POST_TRANSITION),
        e(50,  "Sn", "锡", "Tin",          118.71, POST_TRANSITION),
        e(51,  "Sb", "锑", "Antimony",     121.76, METALLOID),
        e(52,  "Te", "碲", "Tellurium",    127.60, METALLOID),
        e(53,  "I",  "碘", "Iodine",       126.90, HALOGEN),
        e(54,  "Xe", "氙", "Xenon",        131.29, NOBLE_GAS),
        e(55,  "Cs", "铯", "Caesium",      132.91, ALKALI),
        e(56,  "Ba", "钡", "Barium",       137.33, ALKALINE),
        e(57,  "La", "镧", "Lanthanum",    138.91, LANTHANIDE),
        e(58,  "Ce", "铈", "Cerium",       140.12, LANTHANIDE),
        e(59,  "Pr", "镨", "Praseodymium", 140.91, LANTHANIDE),
        e(60,  "Nd", "钕", "Neodymium",    144.24, LANTHANIDE),
        e(61,  "Pm", "钷", "Promethium",   145.0,  LANTHANIDE),
        e(62,  "Sm", "钐", "Samarium",     150.36, LANTHANIDE),
        e(63,  "Eu", "铕", "Europium",     151.96, LANTHANIDE),
        e(64,  "Gd", "钆", "Gadolinium",   157.25, LANTHANIDE),
        e(65,  "Tb", "铽", "Terbium",      158.93, LANTHANIDE),
        e(66,  "Dy", "镝", "Dysprosium",   162.50, LANTHANIDE),
        e(67,  "Ho", "钬", "Holmium",      164.93, LANTHANIDE),
        e(68,  "Er", "铒", "Erbium",       167.26, LANTHANIDE),
        e(69,  "Tm", "铥", "Thulium",      168.93, LANTHANIDE),
        e(70,  "Yb", "镱", "Ytterbium",    173.05, LANTHANIDE),
        e(71,  "Lu", "镥", "Lutetium",     174.97, LANTHANIDE),
        e(72,  "Hf", "铪", "Hafnium",      178.49, TRANSITION),
        e(73,  "Ta", "钽", "Tantalum",     180.95, TRANSITION),
        e(74,  "W",  "钨", "Tungsten",     183.84, TRANSITION),
        e(75,  "Re", "铼", "Rhenium",      186.21, TRANSITION),
        e(76,  "Os", "锇", "Osmium",       190.23, TRANSITION),
        e(77,  "Ir", "铱", "Iridium",      192.22, TRANSITION),
        e(78,  "Pt", "铂", "Platinum",     195.08, TRANSITION),
        e(79,  "Au", "金", "Gold",         196.97, TRANSITION),
        e(80,  "Hg", "汞", "Mercury",      200.59, TRANSITION),
        e(81,  "Tl", "铊", "Thallium",     204.38, POST_TRANSITION),
        e(82,  "Pb", "铅", "Lead",         207.2,  POST_TRANSITION),
        e(83,  "Bi", "铋", "Bismuth",      208.98, POST_TRANSITION),
        e(84,  "Po", "钋", "Polonium",     209.0,  POST_TRANSITION),
        e(85,  "At", "砹", "Astatine",     210.0,  HALOGEN),
        e(86,  "Rn", "氡", "Radon",        222.0,  NOBLE_GAS),
        e(87,  "Fr", "钫", "Francium",     223.0,  ALKALI),
        e(88,  "Ra", "镭", "Radium",       226.0,  ALKALINE),
        e(89,  "Ac", "锕", "Actinium",     227.0,  ACTINIDE),
        e(90,  "Th", "钍", "Thorium",      232.04, ACTINIDE),
        e(91,  "Pa", "镤", "Protactinium", 231.04, ACTINIDE),
        e(92,  "U",  "铀", "Uranium",      238.03, ACTINIDE),
        e(93,  "Np", "镎", "Neptunium",    237.0,  ACTINIDE),
        e(94,  "Pu", "钚", "Plutonium",    244.0,  ACTINIDE),
        e(95,  "Am", "镅", "Americium",    243.0,  ACTINIDE),
        e(96,  "Cm", "锔", "Curium",       247.0,  ACTINIDE),
        e(97,  "Bk", "锫", "Berkelium",    247.0,  ACTINIDE),
        e(98,  "Cf", "锎", "Californium",  251.0,  ACTINIDE),
        e(99,  "Es", "锿", "Einsteinium",  252.0,  ACTINIDE),
        e(100, "Fm", "镄", "Fermium",      257.0,  ACTINIDE),
        e(101, "Md", "钔", "Mendelevium",  258.0,  ACTINIDE),
        e(102, "No", "锘", "Nobelium",     259.0,  ACTINIDE),
        e(103, "Lr", "铹", "Lawrencium",   266.0,  ACTINIDE),
        e(104, "Rf", "𬬻", "Rutherfordium", 267.0, TRANSITION),
        e(105, "Db", "𬬮", "Dubnium",      268.0,  TRANSITION),
        e(106, "Sg", "𬭳", "Seaborgium",   269.0,  TRANSITION),
        e(107, "Bh", "𬭛", "Bohrium",      270.0,  TRANSITION),
        e(108, "Hs", "𬭶", "Hassium",      269.0,  TRANSITION),
        e(109, "Mt", "鿏", "Meitnerium",   278.0,  TRANSITION),
        e(110, "Ds", "𫟼", "Darmstadtium", 281.0,  TRANSITION),
        e(111, "Rg", "𬬭", "Roentgenium",  282.0,  TRANSITION),
        e(112, "Cn", "𬭸", "Copernicium",  285.0,  TRANSITION),
        e(113, "Nh", "鉨", "Nihonium",     286.0,  POST_TRANSITION),
        e(114, "Fl", "𫓧", "Flerovium",    289.0,  POST_TRANSITION),
        e(115, "Mc", "镆", "Moscovium",    290.0,  POST_TRANSITION),
        e(116, "Lv", "𫟷", "Livermorium",  293.0,  POST_TRANSITION),
        e(117, "Ts", "鿬", "Tennessine",   294.0,  HALOGEN),
        e(118, "Og", "鿫", "Oganesson",    294.0,  NOBLE_GAS)
    )

    /** 按符号索引，O(1) 查找 */
    private val bySymbol: Map<String, Element> =
        all.associateBy { it.symbol }

    fun find(symbol: String): Element? = bySymbol[symbol]

    /**
     * 关键词搜索
     *
     * 匹配规则（任一命中即算）：
     * - 输入纯数字：精确匹配原子序数
     * - 输入纯字母：元素符号精确匹配，或英文名包含（不区分大小写）
     * - 输入中文：中文名包含
     * - 空串：返回全部
     */
    fun search(query: String): List<Element> {
        val q = query.trim()
        if (q.isEmpty()) return all

        val n = q.toIntOrNull()
        if (n != null) {
            return all.filter { it.atomicNumber == n }
        }

        return all.filter { el ->
            el.symbol.equals(q, ignoreCase = true) ||
                    el.name.contains(q) ||
                    el.nameEn.contains(q, ignoreCase = true)
        }
    }
}