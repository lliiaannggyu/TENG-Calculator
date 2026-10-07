@file:Suppress("SpellCheckingInspection")

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
import com.tengwear.jisuanqi.logic.ValenceCalculator
import com.tengwear.jisuanqi.ui.theme.*
import kotlin.math.abs

private enum class ValenceStage { EDIT, RESULT }

// ============================ 键盘布局 ============================

/** 元素符号首字母：去掉 J Q X。 */
val ValenceKeyboardUpper: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("A", "A"), KeyDef("B", "B"), KeyDef("C", "C"),
        KeyDef("D", "D"), KeyDef("E", "E"), KeyDef("F", "F")
    ),
    listOf(
        KeyDef("G", "G"), KeyDef("H", "H"), KeyDef("I", "I"),
        KeyDef("K", "K"), KeyDef("L", "L"), KeyDef("M", "M")
    ),
    listOf(
        KeyDef("N", "N"), KeyDef("O", "O"), KeyDef("P", "P"),
        KeyDef("R", "R"), KeyDef("S", "S"), KeyDef("T", "T")
    ),
    listOf(
        KeyDef("U", "U"), KeyDef("V", "V"), KeyDef("W", "W"),
        KeyDef("Y", "Y"), KeyDef("Z", "Z"), KeyDef("\u232B", "\u232B")
    )
)

/** 元素符号第二位小写：去掉 j p q w x z。 */
val ValenceKeyboardLower: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("b", "b"), KeyDef("c", "c"), KeyDef("d", "d"),
        KeyDef("e", "e"), KeyDef("f", "f"), KeyDef("g", "g")
    ),
    listOf(
        KeyDef("h", "h"), KeyDef("i", "i"), KeyDef("k", "k"),
        KeyDef("l", "l"), KeyDef("m", "m"), KeyDef("n", "n")
    ),
    listOf(
        KeyDef("o", "o"), KeyDef("r", "r"), KeyDef("s", "s"),
        KeyDef("t", "t"), KeyDef("u", "u"), KeyDef("v", "v")
    ),
    listOf(
        KeyDef("y", "y"), KeyDef("\u232B", "\u232B"),
        KeyDef("APPLY", "确定")
    )
)

/** 数字 + 括号 + 中点。 */
val ValenceKeyboardDigits: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("1", "1"), KeyDef("2", "2"), KeyDef("3", "3"),
        KeyDef("4", "4"), KeyDef("5", "5"), KeyDef("6", "6")
    ),
    listOf(
        KeyDef("7", "7"), KeyDef("8", "8"), KeyDef("9", "9"),
        KeyDef("0", "0"), KeyDef("(", "("), KeyDef(")", ")")
    ),
    listOf(
        KeyDef("·", "·"),
        KeyDef("\u232B", "\u232B"),
        KeyDef("APPLY", "确定")
    )
)

val ValenceRowWidthsUpper: List<Float> = listOf(1.00f, 1.00f, 1.00f, 1.00f)
val ValenceRowWidthsLower: List<Float> = listOf(1.00f, 1.00f, 1.00f, 0.65f)
val ValenceRowWidthsDigits: List<Float> = listOf(1.00f, 1.00f, 0.65f)

// ============================ 主屏幕 ============================

@Composable
fun ValenceScreen(onDismiss: () -> Unit) {
    var stage by remember { mutableStateOf(ValenceStage.EDIT) }
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<ValenceCalculator.Result?>(null) }

    val columnState = rememberTransformingLazyColumnState()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            when (stage) {
                ValenceStage.EDIT -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    ValenceEditor(
                        initial = input,
                        onConfirm = { text ->
                            input = text
                            result = ValenceCalculator.calculate(text)
                            stage = ValenceStage.RESULT
                        },
                        onCancel = {
                            if (input.isEmpty()) onDismiss()
                            else stage = ValenceStage.RESULT
                        }
                    )
                }

                ValenceStage.RESULT -> ValenceResultPage(
                    input = input,
                    result = result,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onEdit = { stage = ValenceStage.EDIT },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

// ============================ 输入页 ============================

@Composable
private fun ValenceEditor(
    initial: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    val keyboards = remember {
        listOf(ValenceKeyboardUpper, ValenceKeyboardLower, ValenceKeyboardDigits)
    }
    val rowWidthsList = remember {
        listOf(ValenceRowWidthsUpper, ValenceRowWidthsLower, ValenceRowWidthsDigits)
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
                text = if (text.isEmpty()) "例：H2SO4、KMnO4、Fe2O3"
                else text,
                color = if (text.isEmpty()) md_onSurfaceVariant else md_primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            )

            Spacer(Modifier.height(2.dp))

            ValenceKeyboardDots(total = keyboards.size, current = keyboardIndex)

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
                            else -> text += key
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ValenceKeyboardDots(total: Int, current: Int) {
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

// ============================ 结果页 ============================

@Composable
private fun ValenceResultPage(
    input: String,
    result: ValenceCalculator.Result?,
    columnState: TransformingLazyColumnState,
    contentPadding: PaddingValues,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    val spec = rememberTransformationSpec()

    BackHandler(enabled = true) { onDismiss() }

    TransformingLazyColumn(
        state = columnState,
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        item {
            Text(
                text = "化合价计算",
                color = md_primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
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
                    color = md_error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(vertical = 12.dp)
                )
            }
        }

        if (result != null && result.error == null) {
            items(
                count = result.elements.size,
                key = { index -> result.elements[index].symbol }
            ) { index ->
                ElementValenceRow(
                    element = result.elements[index],
                    modifier = Modifier.transformedHeight(this, spec)
                )
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
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

// ============================ 结果条目 ============================

@Composable
private fun ElementValenceRow(
    element: ValenceCalculator.ElementValence,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(calcFunctionBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(calcOperatorBg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = element.symbol,
                color = calcOperatorText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = element.name,
                color = calcFunctionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(
                text = "原子数 ${element.count}",
                color = md_onSurfaceVariant,
                fontSize = 10.sp,
                maxLines = 1
            )
        }

        val (sign, color) = when {
            element.valence > 0 -> "+" to md_primary
            element.valence < 0 -> "" to md_tertiary
            else -> "" to md_onSurfaceVariant
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$sign${element.valence}",
                color = color,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

// ============================ 圆形按钮 ============================

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