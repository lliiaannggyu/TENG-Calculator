package com.tengwear.jisuanqi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.tengwear.jisuanqi.logic.UnitConverterResult
import com.tengwear.jisuanqi.logic.UnitConverterSolver
import com.tengwear.jisuanqi.ui.theme.*
import kotlin.math.abs

private enum class UCStage { EDIT, RESULT }

@Composable
fun UnitConverterScreen(onDismiss: () -> Unit) {
    var stage by remember { mutableStateOf(UCStage.EDIT) }
    var categoryIndex by remember { mutableIntStateOf(0) }
    var sourceIndex by remember { mutableIntStateOf(0) }
    var targetIndex by remember { mutableIntStateOf(1) }   // 默认选第二个作为目标
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<UnitConverterResult?>(null) }

    val columnState = rememberTransformingLazyColumnState()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            when (stage) {
                UCStage.EDIT -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    UCEditor(
                        categoryIndex = categoryIndex,
                        sourceIndex = sourceIndex,
                        targetIndex = targetIndex,
                        input = input,
                        onCategoryChange = { newIdx ->
                            categoryIndex = newIdx
                            sourceIndex = 0
                            targetIndex = if (UnitConverterSolver.categories[newIdx].units.size > 1) 1 else 0
                            input = ""
                        },
                        onSourceChange = { newIdx ->
                            if (newIdx == targetIndex) {
                                // 如果选了目标单位，交换
                                targetIndex = sourceIndex
                            }
                            sourceIndex = newIdx
                        },
                        onTargetChange = { newIdx ->
                            if (newIdx == sourceIndex) {
                                sourceIndex = targetIndex
                            }
                            targetIndex = newIdx
                        },
                        onInputChange = { input = it },
                        onConfirm = {
                            result = UnitConverterSolver.solve(
                                categoryIndex, sourceIndex, input
                            )
                            stage = UCStage.RESULT
                        },
                        onCancel = {
                            if (input.isEmpty()) onDismiss()
                            else stage = UCStage.RESULT
                        }
                    )
                }

                UCStage.RESULT -> UCResultPage(
                    result = result,
                    targetIndex = targetIndex,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onEdit = { stage = UCStage.EDIT },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun UCResultPage(
    result: UnitConverterResult?,
    targetIndex: Int,
    columnState: TransformingLazyColumnState,
    contentPadding: PaddingValues,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    val spec = rememberTransformationSpec()

    TransformingLazyColumn(
        state = columnState,
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 标题
        item {
            Text(
                text = "单位换算 · ${result?.categoryName ?: ""}",
                color = md_primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 4.dp)
            )
        }

        // 错误
        if (result?.error != null) {
            item {
                Text(
                    text = result.error,
                    color = md_tertiary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(vertical = 8.dp)
                )
            }
        }

        // 结果
        if (result != null && result.error == null) {
            val target = result.conversions.getOrNull(targetIndex)

            if (target != null) {
                // 源值
                item {
                    Text(
                        text = "${UnitConverterSolver.formatNum(result.inputValue)} ${result.sourceUnit.symbol}",
                        color = md_onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                            .padding(top = 8.dp)
                    )
                }

                // 等号
                item {
                    Text(
                        text = "=",
                        color = md_onSurfaceVariant,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                    )
                }

                // 目标值（大字）
                item {
                    Text(
                        text = UnitConverterSolver.formatNum(target.second),
                        color = md_tertiary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                    )
                }

                // 目标单位
                item {
                    Text(
                        text = target.first.symbol,
                        color = md_tertiary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                    )
                }
            }
        }

        // 底部按钮
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundBtn("编辑") { onEdit() }
                RoundBtn("返回") { onDismiss() }
            }
        }
    }
}

@Composable
private fun RoundBtn(label: String, onClick: () -> Unit) {
    val fontSize = if (label.length > 1) 11.sp else 16.sp
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(calcFunctionBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = calcFunctionText,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun UCEditor(
    categoryIndex: Int,
    sourceIndex: Int,
    targetIndex: Int,
    input: String,
    onCategoryChange: (Int) -> Unit,
    onSourceChange: (Int) -> Unit,
    onTargetChange: (Int) -> Unit,
    onInputChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val categories = UnitConverterSolver.categories
    val category = categories[categoryIndex]

    BackHandler(enabled = true) { onCancel() }

    val config = LocalConfiguration.current
    val keypadWidth = config.screenWidthDp.dp * 0.95f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ===== 类别行 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(4.dp))
            categories.forEachIndexed { i, c ->
                Chip(
                    label = c.name,
                    selected = i == categoryIndex,
                    onClick = { onCategoryChange(i) }
                )
            }
            Spacer(Modifier.size(4.dp))
        }

        Spacer(Modifier.height(2.dp))

        // ===== 源单位行 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(2.dp))
            Text(
                text = "从",
                color = md_onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.padding(end = 2.dp)
            )
            category.units.forEachIndexed { i, u ->
                Chip(
                    label = u.symbol,
                    selected = i == sourceIndex,
                    onClick = { onSourceChange(i) },
                    small = true
                )
            }
            Spacer(Modifier.size(2.dp))
        }

        Spacer(Modifier.height(2.dp))

        // ===== 目标单位行 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.size(2.dp))
            Text(
                text = "到",
                color = md_onSurfaceVariant,
                fontSize = 10.sp,
                modifier = Modifier.padding(end = 2.dp)
            )
            category.units.forEachIndexed { i, u ->
                Chip(
                    label = u.symbol,
                    selected = i == targetIndex,
                    onClick = { onTargetChange(i) },
                    small = true,
                    accentColor = md_tertiary
                )
            }
            Spacer(Modifier.size(2.dp))
        }

        Spacer(Modifier.height(2.dp))

        // ===== 输入值 =====
        Text(
            text = if (input.isEmpty()) "输入数值" else input,
            color = if (input.isEmpty()) md_onSurfaceVariant else md_primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        )

        // ===== 键盘区（左右滑切换类别）=====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    var totalDx = 0f
                    var totalDy = 0f
                    detectDragGestures(
                        onDragStart = { totalDx = 0f; totalDy = 0f },
                        onDragEnd = {
                            val absDx = abs(totalDx)
                            val absDy = abs(totalDy)
                            if (absDx > absDy && absDx > 60f) {
                                val newIdx = if (totalDx < 0) {
                                    (categoryIndex + 1) % categories.size
                                } else {
                                    (categoryIndex - 1 + categories.size) % categories.size
                                }
                                onCategoryChange(newIdx)
                            }
                            totalDx = 0f
                            totalDy = 0f
                        },
                        onDragCancel = { totalDx = 0f; totalDy = 0f },
                        onDrag = { _: PointerInputChange, dragAmount: Offset ->
                            totalDx += dragAmount.x
                            totalDy += dragAmount.y
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            CalcKeyboardView(
                rows = NumberChineseKeyboard,
                rowWidths = NumberChineseRowWidths,
                keypadWidth = keypadWidth,
                onKeyClick = { key ->
                    when (key) {
                        "APPLY" -> if (input.isNotEmpty()) onConfirm()
                        "\u232B" -> if (input.isNotEmpty()) {
                            onInputChange(input.dropLast(1))
                        }
                        "." -> if (!input.contains(".")) onInputChange(input + ".")
                        "-" -> {
                            val newText = if (input.startsWith("-")) input.substring(1)
                            else "-$input"
                            onInputChange(newText)
                        }
                        else -> if (input.length < 18) onInputChange(input + key)
                    }
                }
            )
        }
    }
}

/**
 * 胶囊按钮
 *
 * @param accentColor 非空时用该颜色作选中色（用于区分目标单位行）
 */
@Composable
private fun Chip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    small: Boolean = false,
    accentColor: androidx.compose.ui.graphics.Color? = null
) {
    val selectedBg = accentColor ?: md_primary
    val selectedFg = if (accentColor != null) md_background else md_onPrimary

    val bg = if (selected) selectedBg else calcFunctionBg
    val fg = if (selected) selectedFg else calcFunctionText

    val hPad = if (small) 9.dp else 11.dp
    val vPad = if (small) 3.dp else 4.dp

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = hPad, vertical = vPad),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}