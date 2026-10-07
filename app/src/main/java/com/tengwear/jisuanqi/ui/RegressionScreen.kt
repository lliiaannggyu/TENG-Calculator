package com.tengwear.jisuanqi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.tengwear.jisuanqi.logic.DataPoint
import com.tengwear.jisuanqi.logic.RegressionResult
import com.tengwear.jisuanqi.logic.RegressionSolver
import com.tengwear.jisuanqi.ui.theme.*

private enum class RegressionMode { LIST, EDIT, RESULT }

@Composable
fun RegressionScreen(onDismiss: () -> Unit) {
    var mode by remember { mutableStateOf(RegressionMode.LIST) }
    var points by remember { mutableStateOf(listOf<DataPoint>()) }
    var editingIndex by remember { mutableStateOf(-1) }
    var result by remember { mutableStateOf<RegressionResult?>(null) }

    val columnState = rememberTransformingLazyColumnState()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            when (mode) {
                RegressionMode.LIST -> RegressionList(
                    points = points,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onAdd = {
                        editingIndex = -1
                        mode = RegressionMode.EDIT
                    },
                    onEdit = { idx ->
                        editingIndex = idx
                        mode = RegressionMode.EDIT
                    },
                    onSolve = {
                        result = RegressionSolver.solve(points)
                        mode = RegressionMode.RESULT
                    },
                    onDismiss = onDismiss
                )

                RegressionMode.EDIT -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    RegressionEditor(
                        initial = if (editingIndex >= 0 && editingIndex < points.size) {
                            val p = points[editingIndex]
                            "${RegressionSolver.num(p.x)},${RegressionSolver.num(p.y)}"
                        } else "",
                        isNew = editingIndex < 0,
                        onConfirm = { text ->
                            val pt = parsePoint(text)
                            if (pt != null) {
                                val newList = points.toMutableList()
                                if (editingIndex >= 0 && editingIndex < newList.size) {
                                    newList[editingIndex] = pt
                                } else {
                                    newList.add(pt)
                                }
                                points = newList
                            }
                            mode = RegressionMode.LIST
                        },
                        onDelete = if (editingIndex >= 0) {
                            {
                                val newList = points.toMutableList()
                                newList.removeAt(editingIndex)
                                points = newList
                                mode = RegressionMode.LIST
                            }
                        } else null,
                        onCancel = { mode = RegressionMode.LIST }
                    )
                }

                RegressionMode.RESULT -> RegressionResultPage(
                    points = points,
                    result = result,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onEdit = { mode = RegressionMode.LIST },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

private fun parsePoint(s: String): DataPoint? {
    val parts = s.split(",")
    if (parts.size != 2) return null
    val x = parts[0].trim().toDoubleOrNull() ?: return null
    val y = parts[1].trim().toDoubleOrNull() ?: return null
    return DataPoint(x, y)
}

@Composable
private fun RegressionList(
    points: List<DataPoint>,
    columnState: TransformingLazyColumnState,
    contentPadding: PaddingValues,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onSolve: () -> Unit,
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
                text = "线性回归",
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
                text = "输入数据点 (x, y)，至少 2 个",
                color = md_onSurfaceVariant,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
            )
        }

        if (points.isEmpty()) {
            item {
                Text(
                    text = "尚未添加数据点",
                    color = md_onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(vertical = 20.dp)
                )
            }
        }

        items(points.size) { idx ->
            val pt = points[idx]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .clip(RoundedCornerShape(12.dp))
                    .background(calcNumberBg)
                    .clickable { onEdit(idx) }
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${idx + 1}.",
                        color = md_onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "(${RegressionSolver.num(pt.x)}, ${RegressionSolver.num(pt.y)})",
                        color = calcNumberText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleBtn("+", calcEqualsBg, calcEqualsText, onAdd)
                CircleBtn("求解", calcOperatorBg, calcOperatorText) {
                    if (points.size >= 2) onSolve()
                }
                CircleBtn("返回", calcFunctionBg, calcFunctionText, onDismiss)
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
    val fontSize = if (label.length > 1) 11.sp else 18.sp
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
private fun RegressionResultPage(
    points: List<DataPoint>,
    result: RegressionResult?,
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
                text = "线性回归",
                color = md_primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 4.dp)
            )
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
        } else if (result != null) {
            // 拟合方程（大字号）
            item {
                Text(
                    text = result.equation,
                    color = md_tertiary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(vertical = 6.dp)
                )
            }

            // 关键指标
            item {
                Text(
                    text = "a = ${RegressionSolver.num(result.a)}",
                    color = md_onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                )
            }
            item {
                Text(
                    text = "b = ${RegressionSolver.num(result.b)}",
                    color = md_onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                )
            }
            item {
                Text(
                    text = "r = ${RegressionSolver.num(result.r)}",
                    color = md_onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                )
            }
            item {
                Text(
                    text = "R\u00B2 = ${RegressionSolver.num(result.r2)}",
                    color = md_onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                )
            }

            item {
                Spacer(Modifier.height(6.dp))
            }

            // 详细步骤
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
                CircleBtn("编辑", calcFunctionBg, calcFunctionText, onEdit)
                CircleBtn("返回", calcEqualsBg, calcEqualsText, onDismiss)
            }
        }
    }
}

@Composable
private fun RegressionEditor(
    initial: String,
    isNew: Boolean,
    onConfirm: (String) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }

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
                text = if (isNew) "添加数据点" else "编辑数据点",
                color = md_primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (text.isEmpty()) "例如 3,5" else "($text)",
                color = if (text.isEmpty()) md_onSurfaceVariant else md_primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )

            Spacer(Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = keypadTopOffset),
                contentAlignment = Alignment.Center
            ) {
                CalcKeyboardView(
                    rows = RegressionKeyboard,
                    rowWidths = RegressionRowWidths,
                    keypadWidth = keypadWidth,
                    onKeyClick = { key ->
                        when (key) {
                            "APPLY" -> if (text.isNotEmpty()) onConfirm(text)
                            "\u232B" -> if (text.isNotEmpty()) text = text.dropLast(1)
                            "," -> if (!text.contains(",")) text += ","
                            else -> text += key
                        }
                    }
                )
            }

            if (onDelete != null) {
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleBtn("删除", calcOperatorBg, calcOperatorText, onDelete)
                }
            }
        }
    }
}