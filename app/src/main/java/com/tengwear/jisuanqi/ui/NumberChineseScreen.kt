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
import com.tengwear.jisuanqi.logic.NumberChineseResult
import com.tengwear.jisuanqi.logic.NumberChineseSolver
import com.tengwear.jisuanqi.ui.theme.*

private enum class NCStage { EDIT, RESULT }

@Composable
fun NumberChineseScreen(onDismiss: () -> Unit) {
    var stage by remember { mutableStateOf(NCStage.EDIT) }
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<NumberChineseResult?>(null) }

    val columnState = rememberTransformingLazyColumnState()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            when (stage) {
                NCStage.EDIT -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    NCEditor(
                        initial = input,
                        onConfirm = { text ->
                            input = text
                            result = NumberChineseSolver.solve(text)
                            stage = NCStage.RESULT
                        },
                        onCancel = {
                            if (input.isEmpty()) onDismiss()
                            else stage = NCStage.RESULT
                        }
                    )
                }

                NCStage.RESULT -> NCResultPage(
                    input = input,
                    result = result,
                    columnState = columnState,
                    contentPadding = contentPadding,
                    onEdit = { stage = NCStage.EDIT },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun NCResultPage(
    input: String,
    result: NumberChineseResult?,
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
                text = "数字与汉字",
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
                fontSize = 12.sp,
                maxLines = 2,
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
            item {
                Text(
                    text = "中文小写",
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
                    text = result.lower,
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
                    text = "财务大写",
                    color = md_secondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, spec)
                        .padding(top = 8.dp)
                )
            }

            item {
                Text(
                    text = result.upper,
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
private fun NCEditor(
    initial: String,
    onConfirm: (String) -> Unit,
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
                text = "输入数字",
                color = md_primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = if (text.isEmpty()) "如 12345.67" else text,
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
                    rows = NumberChineseKeyboard,
                    rowWidths = NumberChineseRowWidths,
                    keypadWidth = keypadWidth,
                    onKeyClick = { key ->
                        when (key) {
                            "APPLY" -> if (text.isNotEmpty()) onConfirm(text)
                            "\u232B" -> if (text.isNotEmpty()) text = text.dropLast(1)
                            "." -> if (!text.contains(".")) text += "."
                            "-" -> {
                                text = if (text.startsWith("-")) text.substring(1)
                                else "-$text"
                            }
                            else -> text += key
                        }
                    }
                )
            }
        }
    }
}