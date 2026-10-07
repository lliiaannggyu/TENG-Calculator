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
import com.tengwear.jisuanqi.logic.InequalitySystemResult
import com.tengwear.jisuanqi.logic.InequalitySystemSolver
import com.tengwear.jisuanqi.ui.theme.*

private enum class ISysMode { LIST, EDIT, RESULT }

@Composable
fun InequalitySystemScreen(onDismiss: () -> Unit) {
    var mode by remember { mutableStateOf(ISysMode.LIST) }
    var inequalities by remember { mutableStateOf(listOf<String>()) }
    var editingIndex by remember { mutableStateOf(-1) }
    var result by remember { mutableStateOf<InequalitySystemResult?>(null) }

    val columnState = rememberTransformingLazyColumnState()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            when (mode) {
                ISysMode.LIST -> ISysList(
                    inequalities = inequalities,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onAdd = {
                        editingIndex = -1
                        mode = ISysMode.EDIT
                    },
                    onEdit = { idx ->
                        editingIndex = idx
                        mode = ISysMode.EDIT
                    },
                    onSolve = {
                        result = InequalitySystemSolver.solve(inequalities)
                        mode = ISysMode.RESULT
                    },
                    onDismiss = onDismiss
                )

                ISysMode.EDIT -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    ISysEditor(
                        initial = if (editingIndex >= 0 && editingIndex < inequalities.size)
                            inequalities[editingIndex] else "",
                        isNew = editingIndex < 0,
                        onConfirm = { text ->
                            val newList = inequalities.toMutableList()
                            if (editingIndex >= 0 && editingIndex < newList.size) {
                                if (text.isBlank()) newList.removeAt(editingIndex)
                                else newList[editingIndex] = text
                            } else {
                                if (text.isNotBlank()) newList.add(text)
                            }
                            inequalities = newList
                            mode = ISysMode.LIST
                        },
                        onDelete = if (editingIndex >= 0) {
                            {
                                val newList = inequalities.toMutableList()
                                newList.removeAt(editingIndex)
                                inequalities = newList
                                mode = ISysMode.LIST
                            }
                        } else null,
                        onCancel = { mode = ISysMode.LIST }
                    )
                }

                ISysMode.RESULT -> ISysResult(
                    inequalities = inequalities,
                    result = result,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onEdit = { mode = ISysMode.LIST },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun ISysList(
    inequalities: List<String>,
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
                text = "不等式组",
                color = md_primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 4.dp)
            )
        }

        if (inequalities.isEmpty()) {
            item {
                Text(
                    text = "尚未添加不等式",
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

        items(inequalities.size) { idx ->
            val eq = inequalities[idx]
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

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircleBtn(label = "+", bg = calcEqualsBg, fg = calcEqualsText) { onAdd() }
                CircleBtn(label = "求解", bg = calcOperatorBg, fg = calcOperatorText) {
                    if (inequalities.isNotEmpty()) onSolve()
                }
                CircleBtn(label = "返回", bg = calcFunctionBg, fg = calcFunctionText) { onDismiss() }
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
private fun ISysResult(
    inequalities: List<String>,
    result: InequalitySystemResult?,
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
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            Text(
                text = "不等式组",
                color = md_primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 4.dp)
            )
        }

        items(inequalities.size) { idx ->
            Text(
                text = "  ${idx + 1}. ${prettyExpression(inequalities[idx])}",
                color = md_onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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

            item {
                Text(
                    text = "解集：${result.solution}",
                    color = md_tertiary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(top = 8.dp)
                )
            }

            item {
                Text(
                    text = result.interval,
                    color = md_tertiary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
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
private fun ISysEditor(
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
        val keypadWidth = screenWidth * 0.96f
        val keypadTopOffset = screenHeight * 0.02f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isNew) "添加不等式" else "编辑不等式",
                color = md_primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (text.isEmpty()) "例如 2x+1>0" else prettyExpression(text),
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

            Spacer(Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = keypadTopOffset),
                contentAlignment = Alignment.Center
            ) {
                CalcKeyboardView(
                    rows = InequalityKeyboard,
                    rowWidths = InequalityRowWidths,
                    keypadWidth = keypadWidth,
                    onKeyClick = { key ->
                        when (key) {
                            "APPLY" -> if (text.isNotEmpty()) onConfirm(text)
                            "\u232B" -> if (text.isNotEmpty()) text = text.dropLast(1)
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
                    CircleBtn(
                        label = "删除",
                        bg = calcOperatorBg,
                        fg = calcOperatorText
                    ) { onDelete() }
                }
            }
        }
    }
}