package com.tengwear.jisuanqi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.tengwear.jisuanqi.logic.ExpressionEvaluator
import com.tengwear.jisuanqi.logic.IntegralResult
import com.tengwear.jisuanqi.logic.IntegralSolver
import com.tengwear.jisuanqi.ui.theme.*
import kotlin.math.abs

private enum class IntegralStage { MAIN, EDIT_FX, EDIT_A, EDIT_B, RESULT }

@Composable
fun IntegralScreen(onDismiss: () -> Unit) {
    var fx by remember { mutableStateOf("") }
    var aStr by remember { mutableStateOf("") }
    var bStr by remember { mutableStateOf("") }
    var stage by remember { mutableStateOf(IntegralStage.MAIN) }
    var result by remember { mutableStateOf<IntegralResult?>(null) }

    val columnState = rememberTransformingLazyColumnState()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            when (stage) {
                IntegralStage.MAIN -> IntegralMain(
                    fx = fx,
                    aStr = aStr,
                    bStr = bStr,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onEditFx = { stage = IntegralStage.EDIT_FX },
                    onEditA = { stage = IntegralStage.EDIT_A },
                    onEditB = { stage = IntegralStage.EDIT_B },
                    onCalculate = {
                        val a = parseNum(aStr)
                        val b = parseNum(bStr)
                        if (a != null && b != null && fx.isNotBlank()) {
                            result = IntegralSolver.solve(fx, a, b)
                            stage = IntegralStage.RESULT
                        }
                    },
                    onDismiss = onDismiss
                )

                IntegralStage.EDIT_FX -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    IntegralEditor(
                        initial = fx,
                        title = "被积函数 f(x)",
                        isFunction = true,
                        onConfirm = { fx = it; stage = IntegralStage.MAIN },
                        onCancel = { stage = IntegralStage.MAIN }
                    )
                }

                IntegralStage.EDIT_A -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    IntegralEditor(
                        initial = aStr,
                        title = "下限 a",
                        isFunction = false,
                        onConfirm = { aStr = it; stage = IntegralStage.MAIN },
                        onCancel = { stage = IntegralStage.MAIN }
                    )
                }

                IntegralStage.EDIT_B -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    IntegralEditor(
                        initial = bStr,
                        title = "上限 b",
                        isFunction = false,
                        onConfirm = { bStr = it; stage = IntegralStage.MAIN },
                        onCancel = { stage = IntegralStage.MAIN }
                    )
                }

                IntegralStage.RESULT -> IntegralResultPage(
                    fx = fx,
                    aStr = aStr,
                    bStr = bStr,
                    result = result,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onEdit = { stage = IntegralStage.MAIN },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

private fun parseNum(s: String): Double? {
    if (s.isBlank()) return null
    s.trim().toDoubleOrNull()?.let { return it }
    return ExpressionEvaluator.evaluate(s, 0.0)
}

@Composable
private fun IntegralMain(
    fx: String,
    aStr: String,
    bStr: String,
    columnState: TransformingLazyColumnState,
    contentPadding: PaddingValues,
    onEditFx: () -> Unit,
    onEditA: () -> Unit,
    onEditB: () -> Unit,
    onCalculate: () -> Unit,
    onDismiss: () -> Unit
) {
    val spec = rememberTransformationSpec()
    val canCalc = fx.isNotBlank() && aStr.isNotBlank() && bStr.isNotBlank()

    TransformingLazyColumn(
        state = columnState,
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text(
                text = "定积分",
                color = md_primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 4.dp)
            )
        }

        item {
            Text(
                text = "\u222B[${if (aStr.isEmpty()) "?" else aStr}, ${if (bStr.isEmpty()) "?" else bStr}] ${if (fx.isEmpty()) "f(x)" else prettyExpression(fx)} dx",
                color = md_onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
            )
        }

        item {
            InputRow(label = "f(x) =", value = fx, onClick = onEditFx)
        }

        item {
            InputRow(label = "下限 a =", value = aStr, onClick = onEditA)
        }

        item {
            InputRow(label = "上限 b =", value = bStr, onClick = onEditB)
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val calcBg = if (canCalc) calcEqualsBg else calcFunctionBg.copy(alpha = 0.5f)
                val calcFg = if (canCalc) calcEqualsText else calcFunctionText.copy(alpha = 0.4f)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(calcBg)
                        .clickable(enabled = canCalc) { onCalculate() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "计算",
                        color = calcFg,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(calcFunctionBg)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "返回",
                        color = calcFunctionText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun InputRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(calcNumberBg)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = md_onSurfaceVariant,
                fontSize = 11.sp,
                modifier = Modifier.width(58.dp)
            )
            Text(
                text = if (value.isEmpty()) "点击输入" else prettyExpression(value),
                color = if (value.isEmpty()) md_onSurfaceVariant.copy(alpha = 0.5f) else calcNumberText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun IntegralResultPage(
    fx: String,
    aStr: String,
    bStr: String,
    result: IntegralResult?,
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
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        item {
            Text(
                text = "定积分",
                color = md_primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 4.dp)
            )
        }

        item {
            Text(
                text = "\u222B[$aStr, $bStr] ${prettyExpression(fx)} dx",
                color = md_onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
            )
        }

        if (result?.error != null) {
            item {
                Text(
                    text = result.error,
                    color = md_tertiary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(vertical = 8.dp)
                )
            }
        }

        if (result?.value != null) {
            item {
                Text(
                    text = "\u2248 ${IntegralSolver.formatNum(result.value)}",
                    color = md_tertiary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(top = 6.dp)
                )
            }

            items(result.steps.size) { idx ->
                Text(
                    text = result.steps[idx],
                    color = md_onSurfaceVariant,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleBtn(label = "编辑", bg = calcFunctionBg, fg = calcFunctionText) { onEdit() }
                CircleBtn(label = "返回", bg = calcEqualsBg, fg = calcEqualsText) { onDismiss() }
            }
        }
    }
}

@Composable
private fun CircleBtn(
    label: String,
    bg: androidx.compose.ui.graphics.Color,
    fg: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val fontSize = if (label.length > 1) 11.sp else 16.sp
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun IntegralEditor(
    initial: String,
    title: String,
    isFunction: Boolean,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    val keyboards = remember { listOf(PlotKeyboardBasic, PlotKeyboardFunc) }
    val rowWidthsList = remember { listOf(PlotRowWidthsBasic, PlotRowWidthsFunc) }
    var keyboardIndex by remember { mutableIntStateOf(0) }

    BackHandler(enabled = true) { onCancel() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val keypadWidth = screenWidth * 0.95f
        val keypadTopOffset = screenHeight * 0.02f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = md_primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (text.isEmpty()) "输入..."
                else if (isFunction) "f(x) = ${prettyExpression(text)}"
                else prettyExpression(text),
                color = if (text.isEmpty()) md_onSurfaceVariant else md_primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(Modifier.height(2.dp))

            KeyboardDots(total = keyboards.size, current = keyboardIndex)

            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = keypadTopOffset)
                    .pointerInput(Unit) {
                        var totalDx = 0f
                        var totalDy = 0f
                        detectDragGestures(
                            onDragStart = {
                                totalDx = 0f
                                totalDy = 0f
                            },
                            onDragEnd = {
                                val absDx = abs(totalDx)
                                val absDy = abs(totalDy)
                                if (absDx > absDy && absDx > 60f) {
                                    keyboardIndex = if (totalDx < 0) {
                                        (keyboardIndex + 1) % keyboards.size
                                    } else {
                                        (keyboardIndex - 1 + keyboards.size) % keyboards.size
                                    }
                                }
                                totalDx = 0f
                                totalDy = 0f
                            },
                            onDragCancel = {
                                totalDx = 0f
                                totalDy = 0f
                            },
                            onDrag = { _: PointerInputChange, dragAmount: Offset ->
                                totalDx += dragAmount.x
                                totalDy += dragAmount.y
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                CalcKeyboardView(
                    rows = keyboards[keyboardIndex],
                    rowWidths = rowWidthsList[keyboardIndex],
                    keypadWidth = keypadWidth,
                    onKeyClick = { key ->
                        when (key) {
                            "APPLY" -> if (text.isNotEmpty()) onConfirm(text)
                            "\u232B" -> if (text.isNotEmpty()) text = text.dropLast(1)
                            else -> text += key
                        }
                    },
                    firstRowScale = 0.88f
                )
            }
        }
    }
}

@Composable
private fun KeyboardDots(total: Int, current: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until total) {
            Box(
                modifier = Modifier
                    .size(if (i == current) 6.dp else 4.dp)
                    .clip(CircleShape)
                    .background(
                        if (i == current) md_primary
                        else md_onSurfaceVariant.copy(alpha = 0.4f)
                    )
            )
        }
    }
}