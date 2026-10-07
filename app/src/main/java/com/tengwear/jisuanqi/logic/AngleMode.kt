package com.tengwear.jisuanqi.logic

import kotlin.math.PI

/**
 * 三角函数角度模式
 *
 * - DEG：角度制，sin(30) = 0.5
 * - RAD：弧度制，sin(π/6) = 0.5
 */
enum class AngleMode(val label: String) {
    DEG("DEG"),
    RAD("RAD");

    /** 用户输入的角度 → 弧度 */
    fun toRadians(angle: Double): Double =
        if (this == DEG) angle * PI / 180.0 else angle

    /** 弧度 → 用户角度 */
    fun fromRadians(rad: Double): Double =
        if (this == DEG) rad * 180.0 / PI else rad

    fun toggle(): AngleMode = if (this == DEG) RAD else DEG
}