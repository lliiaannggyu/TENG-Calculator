package com.tengwear.jisuanqi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.tengwear.jisuanqi.logic.SystemResult
import com.tengwear.jisuanqi.logic.SystemSolver
import com.tengwear.jisuanqi.ui.theme.*
import kotlin.math.abs

private enum class SystemMode { LIST, EDIT, RESULT }

@Composable
fun EquationSystemScreen(onDismiss: () -> Unit) {
    var mode by remember { mutableStateOf(SystemMode.LIST) }
    var equations by remember { mutableStateOf(listOf<String>()) }
    var editingIndex by remember { mutableStateOf(-1) }
    var result by remember { mutableStateOf<SystemResult?>(null) }

    AppScaffold {
        ScreenScaffold(
            timeText = { TimeText() }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (mode) {
                    SystemMode.LIST -> SystemList(
                        equations = equations,
                        onAdd = {
                            editingIndex = -1
                            mode = SystemMode.EDIT
                        },
                        onEdit = { idx ->
                            editingIndex = idx
                            mode = SystemMode.EDIT
                        },
                        onSolve = {
                            result = SystemSolver.solve(equations)
                            mode = SystemMode.RESULT
                        },
                        onDismiss = onDismiss
                    )

                    SystemMode.EDIT -> SystemEditor(
                        initial = if (editingIndex >= 0 && editingIndex < equations.size)
                            equations[editingIndex] else "",
                        isNew = editingIndex < 0,
                        onConfirm = { text ->
                            val newList = equations.toMutableList()
                            if (editingIndex >= 0 && editingIndex < newList.size) {
                                if (text.isBlank()) {
                                    newList.removeAt(editingIndex)
                                } else {
                                    newList[editingIndex] = text
                                }
                            } else {
                                if (text.isNotBlank()) newList.add(text)
                            }
                            equations = newList
                            mode = SystemMode.LIST
                        },
                        onDelete = if (editingIndex >= 0) {
                            {
                                val newList = equations.toMutableList()
                                newList.removeAt(editingIndex)
                                equations = newList
                                mode = SystemMode.LIST
                            }
                        } else null,
                        onCancel = { mode = SystemMode.LIST }
                    )

                    SystemMode.RESULT -> SystemResultPage(
                        equations = equations,
                        result = result,
                        onEdit = { mode = SystemMode.LIST },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemList(
    equations: List<String>,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit,
    onSolve: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "方程组求解",
            color = md_primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (equations.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "尚未添加方程",
                        color = md_onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    itemsIndexed(equations) { idx, eq ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                    text = prettyExpression(eq),
                                    color = calcNumberText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleBtn(label = "+", bg = calcEqualsBg, fg = calcEqualsText) { onAdd() }
            CircleBtn(label = "求解", bg = calcOperatorBg, fg = calcOperatorText) {
                if (equations.isNotEmpty()) onSolve()
            }
            CircleBtn(label = "返回", bg = calcFunctionBg, fg = calcFunctionText) { onDismiss() }
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

/**
 * 方程编辑页 — 双键盘 + 左右滑切换，按钮 5 列尺寸
 */
@Composable
private fun SystemEditor(
    initial: String,
    isNew: Boolean,
    onConfirm: (String) -> Unit,
    onDelete: (() -> Unit)?,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    val keyboards = remember { listOf(SystemKeyboardBasic, SystemKeyboardFunc) }
    val rowWidthsList = remember { listOf(SystemRowWidthsBasic, SystemRowWidthsFunc) }
    var keyboardIndex by remember { mutableIntStateOf(0) }

    BackHandler(enabled = true) { onCancel() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val keypadWidth = screenWidth * 0.94f
        val keypadTopOffset = screenHeight * 0.02f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isNew) "添加方程" else "编辑方程",
                color = md_primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (text.isEmpty()) "例如 2x+3y=8" else prettyExpression(text),
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

            Spacer(Modifier.height(4.dp))

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
                            "=" -> if (!text.contains("=")) text += "="
                            else -> text += key
                        }
                    },
                    // ★ 5 列 → 按钮更大
                    maxColumns = 5
                )
            }

            if (onDelete != null) {
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircleBtn(
                        label = "删除",
                        bg = calcOperatorBg,
                        fg = calcOperatorText
                    ) {
                        onDelete()
                    }
                }
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

@Composable
private fun SystemResultPage(
    equations: List<String>,
    result: SystemResult?,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "方程组求解",
            color = md_primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(equations) { idx, eq ->
                    Text(
                        text = "  方程 ${idx + 1}：${prettyExpression(eq)}",
                        color = md_onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                item { Spacer(Modifier.height(6.dp)) }

                if (result?.error != null) {
                    item {
                        Text(
                            text = result.error,
                            color = md_tertiary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (result != null && result.values.isNotEmpty()) {
                    itemsIndexed(result.steps) { _, line ->
                        Text(
                            text = line,
                            color = md_onSurfaceVariant,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        )
                    }

                    item { Spacer(Modifier.height(6.dp)) }

                    itemsIndexed(result.variables) { idx, name ->
                        Text(
                            text = "$name = ${SystemSolver.num(result.values[idx])}",
                            color = md_tertiary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleBtn(label = "编辑", bg = calcFunctionBg, fg = calcFunctionText) { onEdit() }
            CircleBtn(label = "返回", bg = calcEqualsBg, fg = calcEqualsText) { onDismiss() }
        }
    }
}