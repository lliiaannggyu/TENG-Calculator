package com.tengwear.jisuanqi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.tengwear.jisuanqi.logic.NumberBaseResult
import com.tengwear.jisuanqi.logic.NumberBaseSolver
import com.tengwear.jisuanqi.ui.theme.*

private enum class NBStage { EDIT, RESULT }

@Composable
fun NumberBaseScreen(onDismiss: () -> Unit) {
    var stage by remember { mutableStateOf(NBStage.EDIT) }
    var input by remember { mutableStateOf("") }
    var fromBase by remember { mutableIntStateOf(10) }
    var result by remember { mutableStateOf<NumberBaseResult?>(null) }

    val columnState = rememberTransformingLazyColumnState()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            when (stage) {
                NBStage.EDIT -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    NBEditor(
                        input = input,
                        fromBase = fromBase,
                        onInputChange = { input = it },
                        onBaseChange = {
                            fromBase = it
                            input = ""
                        },
                        onConfirm = {
                            result = NumberBaseSolver.solve(input, fromBase)
                            stage = NBStage.RESULT
                        },
                        onCancel = {
                            if (input.isEmpty()) onDismiss()
                            else stage = NBStage.RESULT
                        }
                    )
                }

                NBStage.RESULT -> NBResultPage(
                    input = input,
                    fromBase = fromBase,
                    result = result,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onEdit = { stage = NBStage.EDIT },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun NBResultPage(
    input: String,
    fromBase: Int,
    result: NumberBaseResult?,
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
        // 标题
        item {
            Text(
                text = "进制转换",
                color = md_primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
                    .padding(vertical = 4.dp)
            )
        }

        // 输入展示
        item {
            Text(
                text = "${NumberBaseSolver.baseLabel(fromBase)}：$input",
                color = md_onSurfaceVariant,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .transformedHeight(this, spec)
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

        // 结果：4 组标签 + 值，直接内联，不用扩展函数
        if (result != null && result.error == null) {
            item {
                Text(
                    text = "十进制",
                    color = md_secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(top = 4.dp)
                )
            }
            item {
                Text(
                    text = result.decimal,
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
                Text(
                    text = "二进制",
                    color = md_secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(top = 4.dp)
                )
            }
            item {
                Text(
                    text = result.binary,
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
                Text(
                    text = "八进制",
                    color = md_secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(top = 4.dp)
                )
            }
            item {
                Text(
                    text = result.octal,
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
                Text(
                    text = "十六进制",
                    color = md_secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(top = 4.dp)
                )
            }
            item {
                Text(
                    text = result.hex,
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
private fun NBEditor(
    input: String,
    fromBase: Int,
    onInputChange: (String) -> Unit,
    onBaseChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    BackHandler(enabled = true) { onCancel() }

    // ★ 用 LocalConfiguration 拿屏幕宽度，避免 BoxWithConstraints
    val config = LocalConfiguration.current
    val screenWidthDp = config.screenWidthDp.dp
    val keypadWidth = screenWidthDp * 0.95f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "进制转换",
            color = md_primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(4.dp))

        // 进制切换按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BaseChip("10", fromBase == 10) { onBaseChange(10) }
            BaseChip("2",  fromBase == 2)  { onBaseChange(2) }
            BaseChip("8",  fromBase == 8)  { onBaseChange(8) }
            BaseChip("16", fromBase == 16) { onBaseChange(16) }
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = if (input.isEmpty()) "输入数字" else input,
            color = if (input.isEmpty()) md_onSurfaceVariant else md_primary,
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
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            CalcKeyboardView(
                rows = NumberBaseKeyboard,
                rowWidths = NumberBaseRowWidths,
                keypadWidth = keypadWidth,
                onKeyClick = { key ->
                    when (key) {
                        "APPLY" -> if (input.isNotEmpty()) onConfirm()
                        "\u232B" -> if (input.isNotEmpty()) {
                            onInputChange(input.dropLast(1))
                        }
                        else -> {
                            val valid = when (fromBase) {
                                2 -> key in "01"
                                8 -> key in "01234567"
                                10 -> key in "0123456789"
                                16 -> key.length == 1 && key[0] in "0123456789ABCDEF"
                                else -> false
                            }
                            if (valid && input.length < 32) {
                                onInputChange(input + key)
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * 进制切换小胶囊
 */
@Composable
private fun BaseChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) md_primary else calcFunctionBg
    val fg = if (selected) md_onPrimary else calcFunctionText
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}