package com.tengwear.jisuanqi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.tengwear.jisuanqi.logic.Element
import com.tengwear.jisuanqi.logic.PeriodicElements
import com.tengwear.jisuanqi.ui.theme.*
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

// ==================== 图表布局常量 ====================
private const val CHART_CELL_DP = 40f
private const val CHART_GAP_DP = 1f
private const val CHART_COLS = 18
private const val CHART_ROWS = 9
private const val CHART_MIN_SCALE = 0.25f
private const val CHART_MAX_SCALE = 8f
private const val ROTARY_ZOOM_SENSITIVITY = 0.002f
private const val CHART_LANTHANIDE_GAP_DP = 12f

@Composable
fun PeriodicTableScreen(onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    var expandedNumber by remember { mutableStateOf<Int?>(null) }
    var chartMode by rememberSaveable { mutableStateOf(false) }

    val filtered = remember(query) { PeriodicElements.search(query) }

    val columnState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()

    // 图表模式下按返回键只退回列表
    BackHandler(enabled = chartMode) {
        chartMode = false
    }

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            if (chartMode) {
                PeriodicChart(
                    onDoubleTap = { chartMode = false },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                )
            } else {
                TransformingLazyColumn(
                    state = columnState,
                    contentPadding = contentPadding,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, spec)
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "元素周期表",
                                color = md_primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = { chartMode = true },
                                shape = CircleShape
                            ) {
                                Text("图表", fontSize = 10.sp)
                            }
                        }
                    }

                    item {
                        SearchBar(
                            query = query,
                            onQueryChange = {
                                query = it
                                expandedNumber = null
                            },
                            onClear = {
                                query = ""
                                expandedNumber = null
                            },
                            modifier = Modifier.transformedHeight(this, spec)
                        )
                    }

                    if (query.isNotEmpty()) {
                        item {
                            Text(
                                text = "命中 ${filtered.size} / ${PeriodicElements.all.size}",
                                color = md_onSurfaceVariant,
                                fontSize = 10.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, spec)
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }

                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                text = "未找到匹配元素",
                                color = md_onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, spec)
                                    .padding(vertical = 24.dp)
                            )
                        }
                    } else {
                        items(filtered.size) { idx ->
                            val el = filtered[idx]
                            ElementRow(
                                element = el,
                                expanded = expandedNumber == el.atomicNumber,
                                onToggle = {
                                    expandedNumber =
                                        if (expandedNumber == el.atomicNumber) null
                                        else el.atomicNumber
                                },
                                modifier = Modifier.transformedHeight(this, spec)
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, spec)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(calcEqualsBg)
                                    .clickable { onDismiss() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "返回",
                                    color = calcEqualsText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== 可拖动 / 缩放的元素周期表图表 ====================

@Composable
private fun PeriodicChart(
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val cellDp = CHART_CELL_DP.dp
    val gapDp = CHART_GAP_DP.dp
    val stepDp = cellDp + gapDp
    val extraGapDp = CHART_LANTHANIDE_GAP_DP.dp

    val contentWidthDp = stepDp * CHART_COLS - gapDp
    val contentHeightDp = stepDp * CHART_ROWS - gapDp + extraGapDp

    val contentWPx = with(density) { contentWidthDp.toPx() }
    val contentHPx = with(density) { contentHeightDp.toPx() }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var viewportSize by remember { mutableStateOf(Size.Zero) }
    var initialized by remember { mutableStateOf(false) }

    val rotaryFocus = remember { FocusRequester() }

    fun clampOffset(raw: Offset, s: Float): Offset {
        val vw = viewportSize.width
        val vh = viewportSize.height
        if (vw <= 0f || vh <= 0f) return raw

        val scaledW = contentWPx * s
        val scaledH = contentHPx * s

        val minX: Float
        val maxX: Float
        if (scaledW > vw) {
            minX = vw - scaledW
            maxX = 0f
        } else {
            minX = (vw - scaledW) / 2f
            maxX = minX
        }

        val minY: Float
        val maxY: Float
        if (scaledH > vh) {
            minY = vh - scaledH
            maxY = 0f
        } else {
            minY = (vh - scaledH) / 2f
            maxY = minY
        }

        return Offset(
            raw.x.coerceIn(minX, maxX),
            raw.y.coerceIn(minY, maxY)
        )
    }

    Box(
        modifier = modifier
            .background(calcNumberBg.copy(alpha = 0.35f))
            .onSizeChanged { size ->
                val newSize = Size(size.width.toFloat(), size.height.toFloat())
                viewportSize = newSize
                if (!initialized && newSize.width > 0f && newSize.height > 0f) {
                    // 宽高同时适配，让整表一眼看全
                    val fitScale = minOf(
                        newSize.width / contentWPx,
                        newSize.height / contentHPx
                    ).coerceIn(CHART_MIN_SCALE, CHART_MAX_SCALE)
                    scale = fitScale
                    offset = Offset(
                        (newSize.width - contentWPx * fitScale) / 2f,
                        (newSize.height - contentHPx * fitScale) / 2f
                    )
                    initialized = true
                    runCatching { rotaryFocus.requestFocus() }
                }
            }
            .onRotaryScrollEvent { event ->
                val delta = event.verticalScrollPixels
                if (delta != 0f && viewportSize.width > 0f) {
                    val newScale = (scale * exp(delta * ROTARY_ZOOM_SENSITIVITY))
                        .coerceIn(CHART_MIN_SCALE, CHART_MAX_SCALE)
                    if (newScale != scale) {
                        val pivot = Offset(
                            viewportSize.width / 2f,
                            viewportSize.height / 2f
                        )
                        val k = newScale / scale
                        offset = pivot * (1f - k) + offset * k
                        scale = newScale
                        offset = clampOffset(offset, scale)
                    }
                }
                true
            }
            .focusRequester(rotaryFocus)
            .focusable()
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    if (viewportSize.width <= 0f) return@detectTransformGestures
                    val newScale = (scale * zoom)
                        .coerceIn(CHART_MIN_SCALE, CHART_MAX_SCALE)
                    val k = newScale / scale
                    offset = centroid * (1f - k) + offset * k + pan
                    scale = newScale
                    offset = clampOffset(offset, scale)
                }
            }
    ) {
        if (initialized) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .size(width = contentWidthDp, height = contentHeightDp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
            ) {
                // ===== 普通元素 =====
                PeriodicElements.all.forEach { el ->
                    val pos = elementGridPosition(el.atomicNumber) ?: return@forEach
                    val (col, row) = pos
                    val xDp = stepDp * (col - 1)
                    val yDp = stepDp * (row - 1) +
                            if (row >= 8) extraGapDp else 0.dp

                    ChartCell(
                        element = el,
                        group = groupNumber(el.atomicNumber),
                        sizeDp = cellDp,
                        modifier = Modifier.offset(x = xDp, y = yDp)
                    )
                }

                // ===== 主表占位块：镧系 =====
                PlaceholderCell(
                    topText = "57-71",
                    bottomText = "镧系",
                    color = categoryColor(PeriodicElements.LANTHANIDE),
                    sizeDp = cellDp,
                    modifier = Modifier.offset(
                        x = stepDp * 2,
                        y = stepDp * 5
                    )
                )

                // ===== 主表占位块：锕系 =====
                PlaceholderCell(
                    topText = "89-103",
                    bottomText = "锕系",
                    color = categoryColor(PeriodicElements.ACTINIDE),
                    sizeDp = cellDp,
                    modifier = Modifier.offset(
                        x = stepDp * 2,
                        y = stepDp * 6
                    )
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        runCatching { rotaryFocus.requestFocus() }
    }
}

// ==================== 元素单元格 ====================

/**
 * 40dp 格子内的精确纵向分布（单位 dp）：
 *
 *   y=1  ┌──────┬──────┐
 *        │  26  │    8 │  序号 / 族号
 *   y=9  │   Fe      │   符号
 *  y=21  │   铁      │   中文名
 *  y=28  │  55.85   │   原子量
 *  y=34  └──────┴──────┘
 *
 * 所有槽位用 Top 锚点 + offset(y)，并显式设置 lineHeight = fontSize，
 * 杜绝行盒意外撑高导致的叠加。
 */
@Composable
private fun ChartCell(
    element: Element,
    group: Int?,
    sizeDp: Dp,
    modifier: Modifier = Modifier
) {
    val color = categoryColor(element.category)

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.22f))
    ) {
        // ===== 序号（左上） =====
        Text(
            text = element.atomicNumber.toString(),
            maxLines = 1,
            style = TextStyle(
                color = color,
                fontSize = 5.sp,
                lineHeight = 5.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 2.dp, y = 1.dp)
        )

        // ===== 族号（右上） =====
        if (group != null) {
            Text(
                text = group.toString(),
                maxLines = 1,
                style = TextStyle(
                    color = color.copy(alpha = 0.6f),
                    fontSize = 4.sp,
                    lineHeight = 4.sp
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-2).dp, y = 1.5.dp)
            )
        }

        // ===== 符号（居中偏上） =====
        Text(
            text = element.symbol,
            maxLines = 1,
            style = TextStyle(
                color = color,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 9.dp)
        )

        // ===== 中文名 =====
        Text(
            text = element.name,
            maxLines = 1,
            style = TextStyle(
                color = color.copy(alpha = 0.9f),
                fontSize = 5.sp,
                lineHeight = 6.sp
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 22.dp)
        )

        // ===== 原子量（底部） =====
        Text(
            text = formatMassCompact(element.atomicMass),
            maxLines = 1,
            style = TextStyle(
                color = color.copy(alpha = 0.7f),
                fontSize = 4.sp,
                lineHeight = 5.sp
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 30.dp)
        )
    }
}

// ==================== 镧系 / 锕系占位单元格 ====================

@Composable
private fun PlaceholderCell(
    topText: String,
    bottomText: String,
    color: Color,
    sizeDp: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.30f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = topText,
            color = color,
            fontSize = 5.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Text(
            text = bottomText,
            color = color.copy(alpha = 0.85f),
            fontSize = 4.sp,
            maxLines = 1
        )
    }
}

// ==================== 搜索栏 ====================

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(percent = 50))
            .background(calcNumberBg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clickable { focusRequester.requestFocus() }
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = calcNumberText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(md_primary),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { keyboardController?.hide() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 20.dp)
                    .focusRequester(focusRequester)
            )

            if (query.isEmpty()) {
                Text(
                    text = "点击搜索（符号 / 中文名 / 序数）",
                    color = md_onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }

        if (query.isNotEmpty()) {
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(calcFunctionBg)
                    .clickable {
                        onClear()
                        keyboardController?.hide()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\u00D7",
                    color = calcFunctionText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ==================== 单个元素行（列表模式） ====================

@Composable
private fun ElementRow(
    element: Element,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColor = categoryColor(element.category)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(calcNumberBg)
            .clickable { onToggle() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = element.atomicNumber.toString(),
                    color = badgeColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = element.symbol,
                color = calcNumberText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.width(42.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = element.name,
                    color = calcNumberText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = element.nameEn,
                    color = md_onSurfaceVariant,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = formatMass(element.atomicMass),
                color = md_onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (expanded) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InfoChip(label = "类别", value = element.category, accent = badgeColor)
                InfoChip(
                    label = "原子量",
                    value = formatMass(element.atomicMass),
                    accent = md_primary
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    value: String,
    accent: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(accent.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$label ", color = md_onSurfaceVariant, fontSize = 10.sp)
        Text(
            text = value,
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ==================== 元素位置推导 ====================

/**
 * 根据原子序数推导周期表网格位置 (列, 行)
 *
 * 列 1..18
 * 行 1..7   = 主表（第 6、7 行的第 3 列空出来给镧系/锕系占位块）
 * 行 8      = 镧系（57-71，从第 3 列开始）
 * 行 9      = 锕系（89-103，从第 3 列开始）
 */
private fun elementGridPosition(z: Int): Pair<Int, Int>? = when (z) {
    1 -> 1 to 1
    2 -> 18 to 1

    3 -> 1 to 2
    4 -> 2 to 2
    in 5..10 -> (13 + (z - 5)) to 2

    11 -> 1 to 3
    12 -> 2 to 3
    in 13..18 -> (13 + (z - 13)) to 3

    19 -> 1 to 4
    20 -> 2 to 4
    in 21..36 -> (z - 18) to 4

    37 -> 1 to 5
    38 -> 2 to 5
    in 39..54 -> (z - 36) to 5

    55 -> 1 to 6
    56 -> 2 to 6
    in 57..71 -> (3 + (z - 57)) to 8
    in 72..86 -> (z - 68) to 6

    87 -> 1 to 7
    88 -> 2 to 7
    in 89..103 -> (3 + (z - 89)) to 9
    in 104..118 -> (z - 100) to 7

    else -> null
}

/**
 * 族号 = 列号（1..18）。
 * 镧系（第 8 行）/ 锕系（第 9 行）不标族号。
 */
private fun groupNumber(z: Int): Int? {
    val pos = elementGridPosition(z) ?: return null
    val (col, row) = pos
    return if (row in 1..7) col else null
}

// ==================== 配色 ====================

private fun categoryColor(category: String): Color = when (category) {
    PeriodicElements.ALKALI          -> Color(0xFFFF6B6B)   // 碱金属 - 红
    PeriodicElements.ALKALINE        -> Color(0xFFFFA94D)   // 碱土金属 - 橙
    PeriodicElements.TRANSITION      -> Color(0xFFFFD43B)   // 过渡金属 - 黄
    PeriodicElements.POST_TRANSITION -> Color(0xFF69DB7C)   // 主族金属 - 绿
    PeriodicElements.METALLOID       -> Color(0xFF4DABF7)   // 类金属 - 蓝
    PeriodicElements.NONMETAL        -> Color(0xFF3BC9DB)   // 非金属 - 青
    PeriodicElements.HALOGEN         -> Color(0xFF9775FA)   // 卤素 - 紫
    PeriodicElements.NOBLE_GAS       -> Color(0xFFF783AC)   // 稀有气体 - 粉
    PeriodicElements.LANTHANIDE      -> Color(0xFFE599F7)   // 镧系 - 淡紫
    PeriodicElements.ACTINIDE        -> Color(0xFFB197FC)   // 锕系 - 紫罗兰
    else                             -> md_primary
}

// ==================== 原子量格式化 ====================

/** 列表模式用 —— 尽量保留精度 */
private fun formatMass(m: Double): String {
    return if (abs(m - m.toInt()) < 1e-6) {
        m.toInt().toString()
    } else {
        val s = String.format("%.3f", m)
        s.trimEnd('0').trimEnd('.')
    }
}

/** 图表单元格用 —— 紧凑，最多两位小数 */
private fun formatMassCompact(m: Double): String {
    return if (abs(m - m.toInt()) < 1e-2) {
        m.toInt().toString()
    } else {
        val s = String.format("%.2f", m)
        s.trimEnd('0').trimEnd('.')
    }
}