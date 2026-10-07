package com.tengwear.jisuanqi.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text
import com.tengwear.jisuanqi.ui.theme.*

data class KeyDef(val key: String, val label: String)

private val FUNCTION_KEYS: Set<String> = setOf(
    "AC", "+/-",
    "sin", "cos", "tan", "asin", "acos", "atan",
    "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
    "ln", "log", "log2", "sqrt", "cbrt",
    "sq", "cube", "pow", "10pow", "2pow", "epow",
    "abs", "recip", "fact", "floor", "ceil",
    "e", "pi",
    "rnd", ",", "%", "Ans", "EXP", "exp", "DMS",
    "M+", "M-", "MR", "MC",
    "(", ")", "\u232B"
)

internal fun colorsForKey(key: String): Pair<Color, Color> {
    if (key == "x") {
        return md_secondaryContainer to md_onSecondaryContainer
    }
    // ★ 新增 "root"
    if (key in setOf("+", "-", "*", "/", "^", "pow", "root")) {
        return calcOperatorBg to calcOperatorText
    }
    if (key == "=" || key == "eq" || key == "APPLY") {
        return calcEqualsBg to calcEqualsText
    }
    if (key.startsWith("const_")) {
        return calcFunctionBg to calcFunctionText
    }
    if (key in FUNCTION_KEYS) {
        return calcFunctionBg to calcFunctionText
    }
    return calcNumberBg to calcNumberText
}

/**
 * 单个按键 —— 按下时宽度变大，松手缩回原宽
 *
 * @param expandRatio 按下时宽度倍数（默认 1.20 = 变宽 20%）
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalcButton(
    label: String,
    key: String,
    baseWidth: Dp,
    height: Dp,
    shape: RoundedCornerShape,
    fontSize: TextUnit,
    expandRatio: Float = 1.20f,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    hapticEnabled: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    val (containerColor, contentColor) = colorsForKey(key)

    // 跟踪「按下」状态
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 按下 → 变宽；松手 → 回原宽
    val targetWidth: Dp = if (isPressed) baseWidth * expandRatio else baseWidth
    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = tween(durationMillis = 110),
        label = "key_press_width"
    )

    fun fireHaptic() {
        if (!hapticEnabled) return
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) {
        }
    }

    Box(
        modifier = Modifier
            .width(animatedWidth)
            .height(height)
            .clip(shape)
            .background(containerColor)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    fireHaptic()
                    onClick()
                },
                onLongClick = onLongClick?.let { cb ->
                    {
                        fireHaptic()
                        cb()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/**
 * 通用键盘渲染器
 */
@Composable
fun CalcKeyboardView(
    rows: List<List<KeyDef>>,
    rowWidths: List<Float>,
    keypadWidth: Dp,
    onKeyClick: (String) -> Unit,
    onKeyLongClick: ((String) -> Unit)? = null,
    maxColumns: Int = 6,
    firstRowScale: Float = 1.0f,
    hapticEnabled: Boolean = false
) {
    val columnUnit: Dp = keypadWidth / maxColumns
    val baseButtonWidth: Dp = columnUnit * 0.92f
    val firstRowButtonWidth: Dp = baseButtonWidth * firstRowScale
    val buttonHeight: Dp = columnUnit * 1.02f * 0.62f
    val rowSpacing: Dp = buttonHeight * 0.15f
    val numberFontSize: TextUnit = (buttonHeight.value * 0.62f).sp
    val smallFontSize: TextUnit = (buttonHeight.value * 0.42f).sp
    val capsuleShape = RoundedCornerShape(percent = 50)

    Column(
        modifier = Modifier.width(keypadWidth),
        verticalArrangement = Arrangement.spacedBy(
            space = rowSpacing,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEachIndexed { rowIndex, row ->
            val rowFraction: Float = rowWidths.getOrElse(rowIndex) { 1.0f }
            val rowButtonWidth: Dp = if (rowIndex == 0) {
                firstRowButtonWidth
            } else {
                baseButtonWidth
            }

            Row(
                modifier = Modifier.fillMaxWidth(rowFraction),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { keyDef ->
                    CalcButton(
                        label = keyDef.label,
                        key = keyDef.key,
                        baseWidth = rowButtonWidth,
                        height = buttonHeight,
                        shape = capsuleShape,
                        fontSize = if (keyDef.label.length > 1) smallFontSize else numberFontSize,
                        expandRatio = 1.20f,
                        onClick = { onKeyClick(keyDef.key) },
                        onLongClick = onKeyLongClick?.let { cb -> { cb(keyDef.key) } },
                        hapticEnabled = hapticEnabled
                    )
                }
            }
        }
    }
}