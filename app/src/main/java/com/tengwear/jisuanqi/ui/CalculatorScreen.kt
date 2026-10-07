package com.tengwear.jisuanqi.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.tengwear.jisuanqi.logic.CalculatorViewModel
import com.tengwear.jisuanqi.logic.HistoryEntry
import com.tengwear.jisuanqi.ui.theme.*
import kotlin.math.abs

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    scaleFactor: Float,
    hapticEnabled: Boolean,
    onSwipeDown: () -> Unit,
    onSwipeUp: () -> Unit
) {
    val state = viewModel.state

    val keyboards = remember { listOf(StandardKeyboard, ScientificKeyboard) }
    var keyboardIndex by remember { mutableIntStateOf(0) }

    val config = LocalConfiguration.current
    val screenWidth: Dp = config.screenWidthDp.dp
    val screenHeight: Dp = config.screenHeightDp.dp

    AppScaffold {
        ScreenScaffold(timeText = { TimeText() }) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keypadWidth: Dp = screenWidth * 0.78f * scaleFactor
                val keypadTopOffset: Dp = screenHeight * 0.06f

                // 是否显示光标：计算中 / 出错时不显示
                val cursorForDisplay = if (state.isCalculating || state.hasError) -1 else state.cursorPos

                DisplayArea(
                    session = viewModel.currentSession,
                    currentDisplay = state.display,
                    currentCursorPos = cursorForDisplay,
                    displayApprox = state.displayApprox,
                    isCalculating = state.isCalculating,
                    calcProgress = state.calcProgress,
                    screenWidth = screenWidth,
                    onCursorChange = { viewModel.setCursorPosition(it) },
                    onHistoryClick = { viewModel.editHistoryEntry(it) },
                    onHistoryLongClick = { viewModel.deleteHistoryEntry(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                KeyboardIndicator(
                    total = keyboards.size,
                    current = keyboardIndex,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(2.4f)
                        .padding(top = keypadTopOffset)
                        .pointerInput(Unit) {
                            var totalDx = 0f
                            var totalDy = 0f
                            detectDragGestures(
                                onDragStart = { totalDx = 0f; totalDy = 0f },
                                onDragEnd = {
                                    val absDx = abs(totalDx)
                                    val absDy = abs(totalDy)
                                    val threshold = 70f
                                    when {
                                        absDx > absDy && absDx > threshold -> {
                                            keyboardIndex = if (totalDx < 0) {
                                                (keyboardIndex + 1) % keyboards.size
                                            } else {
                                                (keyboardIndex - 1 + keyboards.size) % keyboards.size
                                            }
                                        }
                                        absDy > absDx && totalDy > threshold -> onSwipeDown()
                                        absDy > absDx && totalDy < -threshold -> onSwipeUp()
                                    }
                                    totalDx = 0f; totalDy = 0f
                                },
                                onDragCancel = { totalDx = 0f; totalDy = 0f },
                                onDrag = { _, d -> totalDx += d.x; totalDy += d.y }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = keyboardIndex,
                        transitionSpec = {
                            val slideSpec = tween<IntOffset>(durationMillis = 220)
                            val fadeSpec = tween<Float>(durationMillis = 220)
                            if (targetState > initialState) {
                                (slideInHorizontally(animationSpec = slideSpec) { full -> full } +
                                        fadeIn(animationSpec = fadeSpec)) togetherWith
                                        (slideOutHorizontally(animationSpec = slideSpec) { full -> -full } +
                                                fadeOut(animationSpec = fadeSpec))
                            } else {
                                (slideInHorizontally(animationSpec = slideSpec) { full -> -full } +
                                        fadeIn(animationSpec = fadeSpec)) togetherWith
                                        (slideOutHorizontally(animationSpec = slideSpec) { full -> full } +
                                                fadeOut(animationSpec = fadeSpec))
                            }
                        },
                        label = "keyboard_transition"
                    ) { idx ->
                        CalcKeyboardView(
                            rows = keyboards[idx],
                            rowWidths = CalcRowWidths,
                            keypadWidth = keypadWidth,
                            onKeyClick = { handleKey(it, viewModel) },
                            onKeyLongClick = { key ->
                                if (key == "AC") viewModel.onClear()
                            },
                            firstRowScale = 0.88f,
                            hapticEnabled = hapticEnabled
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardIndicator(total: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
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

private fun handleKey(key: String, viewModel: CalculatorViewModel) {
    when (key) {
        "AC" -> viewModel.onBackspace()
        "+/-" -> viewModel.onToggleSign()
        "+", "-", "*", "/" -> viewModel.onOperator(key)
        "=" -> viewModel.onEquals()
        "sin", "cos", "tan", "asin", "acos", "atan",
        "log", "lg", "sqrt", "abs", "%" -> viewModel.onScientificUnary(key)
        "pow" -> viewModel.onOperator("pow")
        "root" -> viewModel.onOperator("root")
        "e" -> viewModel.onConstantE()
        "rnd" -> viewModel.onRandom()
        "Ans" -> viewModel.onAnswer()
        "," -> { /* no-op */ }
        "(" -> viewModel.onInput("(")
        ")" -> viewModel.onInput(")")
        else -> viewModel.onInput(key)
    }
}

/**
 * 显示区：历史视窗 + 当前算式
 *   - 点击历史：把该条的表达式放回输入框
 *   - 长按历史：从当前会话删除该条
 *   - 点击当前算式：定位光标
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DisplayArea(
    session: List<HistoryEntry>,
    currentDisplay: String,
    currentCursorPos: Int,
    displayApprox: Boolean,
    isCalculating: Boolean,
    calcProgress: Float,
    screenWidth: Dp,
    onCursorChange: (Int) -> Unit,
    onHistoryClick: (Int) -> Unit,
    onHistoryLongClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val totalItems = session.size + 1

    LaunchedEffect(totalItems) {
        if (totalItems > 0) listState.animateScrollToItem(totalItems - 1)
    }

    val historyExprSize: TextUnit = (screenWidth.value * 0.038f).sp
    val historyResultSize: TextUnit = (screenWidth.value * 0.060f).sp

    // 字号只分两档，不再缩到极小
    val currentDisplaySize: TextUnit = when {
        currentDisplay.length <= 15 -> (screenWidth.value * 0.135f).sp
        else -> (screenWidth.value * 0.098f).sp
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        itemsIndexed(session) { index, entry ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .combinedClickable(
                        onClick = { onHistoryClick(index) },
                        onLongClick = { onHistoryLongClick(index) }
                    )
                    .padding(vertical = 3.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = entry.expression,
                    color = calcExpressionText.copy(alpha = 0.6f),
                    fontSize = historyExprSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = entry.result,
                    color = calcDisplayText.copy(alpha = 0.75f),
                    fontSize = historyResultSize,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isCalculating) {
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .fillMaxWidth(0.7f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(md_onSurfaceVariant.copy(alpha = 0.25f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(calcProgress.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(50))
                                .background(md_primary)
                        )
                    }
                    Text(
                        text = "计算中… ${(calcProgress * 100).toInt()}%",
                        color = calcExpressionText.copy(alpha = 0.7f),
                        fontSize = (screenWidth.value * 0.032f).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    )
                } else {
                    CurrentDisplay(
                        text = currentDisplay,
                        cursorPos = currentCursorPos,
                        fontSize = currentDisplaySize,
                        displayApprox = displayApprox,
                        onCursorChange = onCursorChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * 当前算式显示：
 *   - 光标为闪烁竖线（不覆盖数字）
 *   - 内容未超屏时居中
 *   - 内容超屏时可横向滚动
 *   - 点击文本可定位光标
 *
 * 关键：TextMeasurer 与 Text 使用同一个 style，
 *       否则字号、字距、字体会不一致，光标越走越偏。
 */
@Composable
private fun CurrentDisplay(
    text: String,
    cursorPos: Int,
    fontSize: TextUnit,
    displayApprox: Boolean,
    onCursorChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val showCursor = cursorPos >= 0

    // ★ 唯一的样式：TextMeasurer 和 Text 都用它
    val style = remember(fontSize) { TextStyle(fontSize = fontSize) }

    // 纯文本整体布局
    val textLayout = remember(text, style) {
        textMeasurer.measure(AnnotatedString(text), style)
    }
    val textWidthPx = textLayout.size.width.toFloat()

    // 光标 x 坐标（相对文本内容左边缘）
    val cursorX = remember(text, cursorPos, style) {
        if (cursorPos <= 0) 0f
        else textMeasurer.measure(
            AnnotatedString(text.substring(0, cursorPos.coerceAtMost(text.length))),
            style
        ).size.width.toFloat()
    }

    // "≈ " 前缀宽度（用同一 style，缓存）
    val prefixWidthPx = remember(style, displayApprox) {
        if (displayApprox) {
            textMeasurer.measure(AnnotatedString("≈ "), style).size.width.toFloat()
        } else 0f
    }

    // 光标闪烁（900ms 一明一暗）
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    // 光标移动时自动滚入视野
    LaunchedEffect(cursorPos, text) {
        val viewport = scrollState.viewportSize
        if (viewport > 0 && cursorX > 0f) {
            val maxScrollF = scrollState.maxValue.toFloat()
            val target: Float = (cursorX - viewport / 3f).coerceIn(0f, maxScrollF)
            scrollState.animateScrollTo(target.toInt())
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val viewportWidthPx = with(density) { maxWidth.toPx() }
        val totalWidthPx = prefixWidthPx + textWidthPx
        val needScroll = totalWidthPx > viewportWidthPx

        val rowModifier = if (needScroll) {
            Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        } else {
            Modifier.fillMaxWidth()
        }
        val rowArrangement = if (needScroll) Arrangement.Start else Arrangement.Center

        Row(
            modifier = rowModifier,
            horizontalArrangement = rowArrangement,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (displayApprox) {
                // ★ 前缀也用同一个 style
                Text(
                    text = "≈ ",
                    style = style,
                    color = calcDisplayText.copy(alpha = 0.75f)
                )
            }

            Text(
                text = text,
                style = style,           // ★ 关键：传 style，不传 fontSize
                color = calcDisplayText,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .drawBehind {
                        if (showCursor) {
                            // 右偏 2dp 避免贴住前一个字符；光标在最右时左偏
                            val offsetPx = 2.dp.toPx()
                            val rawX = if (cursorX >= size.width) cursorX - offsetPx
                            else cursorX + offsetPx
                            val x = rawX.coerceIn(0f, size.width)
                            drawLine(
                                color = md_primary.copy(alpha = cursorAlpha),
                                start = Offset(x, size.height * 0.18f),
                                end = Offset(x, size.height * 0.82f),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                    }
                    .pointerInput(text, showCursor) {
                        if (showCursor) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val idx = textLayout.getOffsetForPosition(offset)
                                    onCursorChange(idx.coerceIn(0, text.length))
                                }
                            )
                        }
                    }
            )
        }
    }
}