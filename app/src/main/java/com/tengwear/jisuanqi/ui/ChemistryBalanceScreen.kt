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
import com.tengwear.jisuanqi.logic.ChemistryResult
import com.tengwear.jisuanqi.logic.ChemistrySolver
import com.tengwear.jisuanqi.ui.theme.*
import kotlin.math.abs

private enum class ChemStage { EDIT, RESULT }

@Composable
fun ChemistryBalanceScreen(
    onDismiss: () -> Unit,
    onOpenEggUpper: () -> Unit = {},
    onOpenEggLower: () -> Unit = {}
) {
    var stage by remember { mutableStateOf(ChemStage.EDIT) }
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ChemistryResult?>(null) }

    val columnState = rememberTransformingLazyColumnState()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            when (stage) {
                ChemStage.EDIT -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    ChemEditor(
                        initial = input,
                        onEggUpperDetected = { onOpenEggUpper() },
                        onEggLowerDetected = { onOpenEggLower() },
                        onConfirm = { text ->
                            input = text
                            result = ChemistrySolver.balance(text)
                            stage = ChemStage.RESULT
                        },
                        onCancel = {
                            if (input.isEmpty()) onDismiss()
                            else stage = ChemStage.RESULT
                        }
                    )
                }

                ChemStage.RESULT -> ChemResultPage(
                    input = input,
                    result = result,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onEdit = { stage = ChemStage.EDIT },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun ChemResultPage(
    input: String,
    result: ChemistryResult?,
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
                text = "化学配平",
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
                text = "输入：$input",
                color = md_onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 3,
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

        if (result != null && result.error == null) {
            item {
                Text(
                    text = "配平结果",
                    color = md_secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(top = 6.dp)
                )
            }

            item {
                Text(
                    text = result.balanced,
                    color = md_tertiary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                )
            }

            item {
                Spacer(Modifier.height(6.dp))
            }

            items(result.steps.size) { idx ->
                Text(
                    text = result.steps[idx],
                    color = md_onSurfaceVariant,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
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
private fun ChemEditor(
    initial: String,
    onEggUpperDetected: () -> Unit,
    onEggLowerDetected: () -> Unit,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    val keyboards = remember {
        listOf(ChemKeyboardNumbers, ChemKeyboardLetters, ChemKeyboardLower)
    }
    val rowWidthsList = remember {
        listOf(ChemRowWidthsNumbers, ChemRowWidthsLetters, ChemRowWidthsLower)
    }
    var keyboardIndex by remember { mutableIntStateOf(0) }

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
                text = if (text.isEmpty()) "例：Fe + O2 = Fe2O3"
                else text,
                color = if (text.isEmpty()) md_onSurfaceVariant else md_primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
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
                            "CLEAR" -> text = ""
                            "=" -> if (!text.contains("=")) text += "="
                            else -> {
                                text += key
                                // ★ 区分大小写实时检测彩蛋
                                val t = text.trim()
                                if (t == "TENG") {
                                    onEggUpperDetected()
                                } else if (t == "teng") {
                                    onEggLowerDetected()
                                }
                            }
                        }
                    }
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