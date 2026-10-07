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
import com.tengwear.jisuanqi.logic.MonotonicityResult
import com.tengwear.jisuanqi.logic.MonotonicitySolver
import com.tengwear.jisuanqi.ui.theme.*
import kotlin.math.abs

private enum class MonoMode { EDIT, RESULT }

@Composable
fun MonotonicityScreen(onDismiss: () -> Unit) {
    var mode by remember { mutableStateOf(MonoMode.EDIT) }
    var expr by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<MonotonicityResult?>(null) }

    val columnState = rememberTransformingLazyColumnState()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            when (mode) {
                MonoMode.EDIT -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    MonoEditor(
                        initial = expr,
                        onConfirm = { text ->
                            expr = text
                            result = MonotonicitySolver.solve(text)
                            mode = MonoMode.RESULT
                        },
                        onCancel = {
                            if (expr.isEmpty()) onDismiss()
                            else mode = MonoMode.RESULT
                        }
                    )
                }

                MonoMode.RESULT -> MonoResult(
                    expr = expr,
                    result = result,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onEdit = { mode = MonoMode.EDIT },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun MonoResult(
    expr: String,
    result: MonotonicityResult?,
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
                text = "单调性分析",
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
                text = "y = ${prettyExpression(expr)}",
                color = md_onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
            )
        }

        if (result?.derivativeDisplay?.isNotEmpty() == true) {
            item {
                Text(
                    text = "y\u2032 = ${result.derivativeDisplay}",
                    color = md_secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                )
            }
        }

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

        if (result != null && result.error == null) {
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

            if (result.intervals.isNotEmpty()) {
                item {
                    Text(
                        text = "单调区间",
                        color = md_primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                            .padding(top = 8.dp)
                    )
                }
                items(result.intervals.size) { idx ->
                    val iv = result.intervals[idx]
                    Text(
                        text = MonotonicitySolver.intervalDescription(iv),
                        color = if (iv.increasing) md_tertiary else md_secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                    )
                }
            }

            if (result.extrema.isNotEmpty()) {
                item {
                    Text(
                        text = "极值点",
                        color = md_primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                            .padding(top = 8.dp)
                    )
                }
                items(result.extrema.size) { idx ->
                    val e = result.extrema[idx]
                    val type = if (e.isMax) "极大值" else "极小值"
                    Text(
                        text = "x = ${MonotonicitySolver.num(e.x)}  \u2192  $type y = ${MonotonicitySolver.num(e.y)}",
                        color = md_tertiary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                    )
                }
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
                RoundIconButton(label = "编辑") { onEdit() }
                RoundIconButton(label = "返回") { onDismiss() }
            }
        }
    }
}

@Composable
private fun RoundIconButton(label: String, onClick: () -> Unit) {
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
private fun MonoEditor(
    initial: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    val plotKeyboards = remember { listOf(PlotKeyboardBasic, PlotKeyboardFunc) }
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
                text = if (text.isEmpty()) "输入函数，如 x^2-3x+2" else "y = ${prettyExpression(text)}",
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

            KeyboardDots(total = plotKeyboards.size, current = keyboardIndex)

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
                                        (keyboardIndex + 1) % plotKeyboards.size
                                    } else {
                                        (keyboardIndex - 1 + plotKeyboards.size) % plotKeyboards.size
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
                    rows = plotKeyboards[keyboardIndex],
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