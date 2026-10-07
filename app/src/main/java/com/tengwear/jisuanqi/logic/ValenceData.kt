package com.tengwear.jisuanqi.logic

data class ValenceElement(
    val atomicNumber: Int,
    val symbol: String,
    val name: String,
    val valences: List<Int>
)

/**
 * 常见元素化合价（按原子序数升序）。
 * 覆盖中学 / 大学化学常用元素，共 66 种。
 */
object ValenceData {

    val elements: List<ValenceElement> = listOf(
        ValenceElement(1,  "H",  "氢", listOf(1, -1)),
        ValenceElement(2,  "He", "氦", listOf(0)),
        ValenceElement(3,  "Li", "锂", listOf(1)),
        ValenceElement(4,  "Be", "铍", listOf(2)),
        ValenceElement(5,  "B",  "硼", listOf(3)),
        ValenceElement(6,  "C",  "碳", listOf(4, 2, -4)),
        ValenceElement(7,  "N",  "氮", listOf(5, 3, -3)),
        ValenceElement(8,  "O",  "氧", listOf(-2, -1)),
        ValenceElement(9,  "F",  "氟", listOf(-1)),
        ValenceElement(10, "Ne", "氖", listOf(0)),
        ValenceElement(11, "Na", "钠", listOf(1)),
        ValenceElement(12, "Mg", "镁", listOf(2)),
        ValenceElement(13, "Al", "铝", listOf(3)),
        ValenceElement(14, "Si", "硅", listOf(4, -4)),
        ValenceElement(15, "P",  "磷", listOf(5, 3, -3)),
        ValenceElement(16, "S",  "硫", listOf(6, 4, -2)),
        ValenceElement(17, "Cl", "氯", listOf(7, 5, 3, 1, -1)),
        ValenceElement(18, "Ar", "氩", listOf(0)),
        ValenceElement(19, "K",  "钾", listOf(1)),
        ValenceElement(20, "Ca", "钙", listOf(2)),
        ValenceElement(21, "Sc", "钪", listOf(3)),
        ValenceElement(22, "Ti", "钛", listOf(4, 3)),
        ValenceElement(23, "V",  "钒", listOf(5, 4, 3, 2)),
        ValenceElement(24, "Cr", "铬", listOf(6, 3, 2)),
        ValenceElement(25, "Mn", "锰", listOf(7, 6, 4, 2)),
        ValenceElement(26, "Fe", "铁", listOf(3, 2)),
        ValenceElement(27, "Co", "钴", listOf(3, 2)),
        ValenceElement(28, "Ni", "镍", listOf(3, 2)),
        ValenceElement(29, "Cu", "铜", listOf(2, 1)),
        ValenceElement(30, "Zn", "锌", listOf(2)),
        ValenceElement(31, "Ga", "镓", listOf(3)),
        ValenceElement(32, "Ge", "锗", listOf(4, 2, -4)),
        ValenceElement(33, "As", "砷", listOf(5, 3, -3)),
        ValenceElement(34, "Se", "硒", listOf(6, 4, -2)),
        ValenceElement(35, "Br", "溴", listOf(7, 5, 3, 1, -1)),
        ValenceElement(36, "Kr", "氪", listOf(0)),
        ValenceElement(37, "Rb", "铷", listOf(1)),
        ValenceElement(38, "Sr", "锶", listOf(2)),
        ValenceElement(40, "Zr", "锆", listOf(4)),
        ValenceElement(41, "Nb", "铌", listOf(5, 3)),
        ValenceElement(42, "Mo", "钼", listOf(6, 4)),
        ValenceElement(44, "Ru", "钌", listOf(8, 4, 3)),
        ValenceElement(45, "Rh", "铑", listOf(3)),
        ValenceElement(46, "Pd", "钯", listOf(4, 2)),
        ValenceElement(47, "Ag", "银", listOf(1)),
        ValenceElement(48, "Cd", "镉", listOf(2)),
        ValenceElement(49, "In", "铟", listOf(3)),
        ValenceElement(50, "Sn", "锡", listOf(4, 2)),
        ValenceElement(51, "Sb", "锑", listOf(5, 3, -3)),
        ValenceElement(52, "Te", "碲", listOf(6, 4, -2)),
        ValenceElement(53, "I",  "碘", listOf(7, 5, 3, 1, -1)),
        ValenceElement(55, "Cs", "铯", listOf(1)),
        ValenceElement(56, "Ba", "钡", listOf(2)),
        ValenceElement(57, "La", "镧", listOf(3)),
        ValenceElement(58, "Ce", "铈", listOf(4, 3)),
        ValenceElement(72, "Hf", "铪", listOf(4)),
        ValenceElement(73, "Ta", "钽", listOf(5)),
        ValenceElement(74, "W",  "钨", listOf(6, 4)),
        ValenceElement(75, "Re", "铼", listOf(7, 4)),
        ValenceElement(76, "Os", "锇", listOf(8, 4)),
        ValenceElement(77, "Ir", "铱", listOf(4, 3)),
        ValenceElement(78, "Pt", "铂", listOf(4, 2)),
        ValenceElement(79, "Au", "金", listOf(3, 1)),
        ValenceElement(80, "Hg", "汞", listOf(2, 1)),
        ValenceElement(82, "Pb", "铅", listOf(4, 2)),
        ValenceElement(92, "U",  "铀", listOf(6, 4))
    )
}