package com.tengwear.jisuanqi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.tengwear.jisuanqi.logic.ExpressionEvaluator
import com.tengwear.jisuanqi.ui.theme.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt

private enum class PlotMode { PLOT, EDIT_EXPR, INPUT_X, INPUT_Y }

private fun niceStep(range: Double, targetTicks: Int): Double {
    if (range <= 0.0 || targetTicks <= 0) return 1.0
    val raw = range / targetTicks
    if (raw <= 0.0) return 1.0
    val exp = floor(log10(raw))
    val magnitude = 10.0.pow(exp)
    val norm = raw / magnitude
    val niceNorm = when {
        norm < 1.5 -> 1.0
        norm < 3.5 -> 2.0
        norm < 7.5 -> 5.0
        else -> 10.0
    }
    return niceNorm * magnitude
}

private fun formatTick(v: Double): String {
    return if (abs(v - v.roundToInt()) < 1e-9) {
        v.roundToInt().toString()
    } else {
        val s = String.format("%.2f", v)
        s.trimEnd('0').trimEnd('.')
    }
}

private fun solveForY(
    expression: String,
    targetY: Double,
    xCenter: Double,
    xRange: Float
): List<Double> {
    val samples = 600
    val solutions = ArrayList<Double>()
    val dedupe = 1e-3
    val xMin = xCenter - xRange
    val xMax = xCenter + xRange

    var prevX = xMin
    var prevF: Double? = ExpressionEvaluator.evaluate(expression, prevX)?.minus(targetY)

    for (i in 1..samples) {
        val x = xMin + (xMax - xMin) * i / samples
        val fRaw = ExpressionEvaluator.evaluate(expression, x)
        val f: Double? = if (fRaw == null || fRaw.isNaN() || fRaw.isInfinite()) {
            null
        } else {
            fRaw - targetY
        }

        val pF: Double? = prevF
        val cF: Double? = f

        if (pF != null && cF != null) {
            var root: Double? = null
            if (pF == 0.0) {
                root = prevX
            } else if (pF * cF < 0.0) {
                var lo = prevX
                var hi = x
                var flo: Double = pF
                repeat(40) {
                    val mid = (lo + hi) / 2
                    val fmidRaw = ExpressionEvaluator.evaluate(expression, mid)
                    val fmid: Double? = if (fmidRaw == null || fmidRaw.isNaN() || fmidRaw.isInfinite()) {
                        null
                    } else {
                        fmidRaw - targetY
                    }
                    if (fmid != null) {
                        if (flo * fmid <= 0.0) {
                            hi = mid
                        } else {
                            lo = mid
                            flo = fmid
                        }
                    }
                }
                root = (lo + hi) / 2
            }

            val r = root
            if (r != null) {
                val dup = solutions.any { abs(it - r) < dedupe }
                if (!dup) solutions.add(r)
            }
        }

        prevX = x
        prevF = f
    }

    return solutions
}

@Composable
fun FunctionPlotScreen(
    expression: String,
    onExpressionChange: (String) -> Unit,
    xRange: Float,
    onXRangeChange: (Float) -> Unit,
    scaleFactor: Float,
    onDismiss: () -> Unit
) {
    var mode by remember { mutableStateOf(PlotMode.PLOT) }
    var isFullscreen by remember { mutableStateOf(false) }

    var selectedX by remember { mutableStateOf<Double?>(null) }
    var selectedY by remember { mutableStateOf<Double?>(null) }

    var targetY by remember { mutableStateOf<Double?>(null) }
    var ySolutions by remember { mutableStateOf<List<Double>>(emptyList()) }

    var xCenter by remember { mutableDoubleStateOf(0.0) }
    var yCenter by remember { mutableDoubleStateOf(0.0) }

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
                    PlotMode.PLOT -> PlotScreen(
                        expression = expression,
                        xRange = xRange,
                        xCenter = xCenter,
                        yCenter = yCenter,
                        onPan = { dxWorld, dyWorld ->
                            xCenter = (xCenter - dxWorld).coerceIn(-1e9, 1e9)
                            yCenter = (yCenter + dyWorld).coerceIn(-1e9, 1e9)
                        },
                        onXRangeChange = onXRangeChange,
                        selectedX = selectedX,
                        selectedY = selectedY,
                        targetY = targetY,
                        ySolutions = ySolutions,
                        isFullscreen = isFullscreen,
                        onFullscreenChange = { isFullscreen = it },
                        onOpenEditor = { mode = PlotMode.EDIT_EXPR },
                        onOpenXInput = { mode = PlotMode.INPUT_X },
                        onOpenYInput = { mode = PlotMode.INPUT_Y },
                        onDismiss = onDismiss
                    )

                    PlotMode.EDIT_EXPR -> ExpressionEditor(
                        initial = expression,
                        scaleFactor = scaleFactor,
                        onConfirm = {
                            onExpressionChange(it)
                            targetY = null
                            ySolutions = emptyList()
                            selectedX = null
                            selectedY = null
                            xCenter = 0.0
                            yCenter = 0.0
                            mode = PlotMode.PLOT
                        },
                        onCancel = { mode = PlotMode.PLOT }
                    )

                    PlotMode.INPUT_X -> NumberInputScreen(
                        initial = "",
                        label = "x =",
                        onConfirm = { text ->
                            val xv = text.toDoubleOrNull()
                            if (xv != null) {
                                selectedX = xv
                                selectedY = ExpressionEvaluator.evaluate(expression, xv)
                                targetY = null
                                ySolutions = emptyList()
                            }
                            mode = PlotMode.PLOT
                        },
                        onCancel = { mode = PlotMode.PLOT }
                    )

                    PlotMode.INPUT_Y -> NumberInputScreen(
                        initial = "",
                        label = "y =",
                        onConfirm = { text ->
                            val yv = text.toDoubleOrNull()
                            if (yv != null) {
                                targetY = yv
                                ySolutions = solveForY(expression, yv, xCenter, xRange)
                                selectedX = null
                                selectedY = null
                            }
                            mode = PlotMode.PLOT
                        },
                        onCancel = { mode = PlotMode.PLOT }
                    )
                }
            }
        }
    }
}

private fun computeYRange(
    expression: String,
    xCenter: Double,
    xRange: Float
): Double {
    var maxAbsY = 0.0
    for (i in 0..300) {
        val x = (xCenter - xRange) + (2.0 * xRange) * i / 300
        val y = ExpressionEvaluator.evaluate(expression, x) ?: continue
        val ay = abs(y)
        if (ay < 1e6) maxAbsY = max(maxAbsY, ay)
    }
    return if (maxAbsY < 1e-6) 1.0 else maxAbsY * 1.1
}

@Composable
private fun PlotScreen(
    expression: String,
    xRange: Float,
    xCenter: Double,
    yCenter: Double,
    onPan: (dxWorld: Double, dyWorld: Double) -> Unit,
    onXRangeChange: (Float) -> Unit,
    selectedX: Double?,
    selectedY: Double?,
    targetY: Double?,
    ySolutions: List<Double>,
    isFullscreen: Boolean,
    onFullscreenChange: (Boolean) -> Unit,
    onOpenEditor: () -> Unit,
    onOpenXInput: () -> Unit,
    onOpenYInput: () -> Unit,
    onDismiss: () -> Unit
) {
    val yRange = remember(expression, xRange) {
        computeYRange(expression, 0.0, xRange)
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isFullscreen) {
        if (isFullscreen) {
            try { focusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    BackHandler(enabled = isFullscreen) {
        onFullscreenChange(false)
    }

    if (isFullscreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onRotaryScrollEvent { event ->
                    val delta = event.verticalScrollPixels
                    val factor = 1f - delta / 800f
                    onXRangeChange((xRange * factor).coerceIn(0.1f, 1000f))
                    true
                }
                .focusRequester(focusRequester)
                .focusable()
        ) {
            PlotCanvas(
                expression = expression,
                xRange = xRange,
                yRange = yRange,
                xCenter = xCenter,
                yCenter = yCenter,
                selectedX = selectedX,
                selectedY = selectedY,
                targetY = targetY,
                ySolutions = ySolutions,
                onPan = { dxPx, dyPx, wPx, hPx ->
                    val dxWorld = dxPx * 2.0 * xRange / wPx
                    val dyWorld = dyPx * 2.0 * yRange / hPx
                    onPan(dxWorld, dyWorld)
                },
                onCanvasClick = { },
                onDoubleTap = { onFullscreenChange(false) },
                modifier = Modifier.fillMaxSize()
            )

            Text(
                text = "y = $expression  ·  拖动平移 · 双击退出",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                color = md_primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "y = $expression",
                color = md_primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            if (selectedX != null && selectedY != null) {
                Text(
                    text = "(${formatTick(selectedX)}, ${formatTick(selectedY)})",
                    color = md_tertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (targetY != null) {
                val txt = if (ySolutions.isEmpty()) {
                    "y = ${formatTick(targetY)} · 无解"
                } else {
                    "y = ${formatTick(targetY)} · ${ySolutions.size} 个解"
                }
                Text(
                    text = txt,
                    color = md_tertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                PlotCanvas(
                    expression = expression,
                    xRange = xRange,
                    yRange = yRange,
                    xCenter = xCenter,
                    yCenter = yCenter,
                    selectedX = selectedX,
                    selectedY = selectedY,
                    targetY = targetY,
                    ySolutions = ySolutions,
                    onPan = null,
                    onCanvasClick = { onFullscreenChange(true) },
                    onDoubleTap = { },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconButton(label = "编辑") { onOpenEditor() }
                RoundIconButton(label = "x 值") { onOpenXInput() }
                RoundIconButton(label = "y 值") { onOpenYInput() }
                RoundIconButton(label = "返回") { onDismiss() }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun RoundIconButton(label: String, onClick: () -> Unit) {
    val fontSize = if (label.length > 1) 11.sp else 16.sp
    Box(
        modifier = Modifier
            .size(40.dp)
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
private fun PlotCanvas(
    expression: String,
    xRange: Float,
    yRange: Double,
    xCenter: Double,
    yCenter: Double,
    selectedX: Double?,
    selectedY: Double?,
    targetY: Double?,
    ySolutions: List<Double>,
    onPan: ((dxPx: Float, dyPx: Float, wPx: Float, hPx: Float) -> Unit)?,
    onCanvasClick: () -> Unit,
    onDoubleTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lineColor = md_primary
    val axisColor = md_onSurfaceVariant.copy(alpha = 0.55f)
    val tickColor = md_onSurfaceVariant.copy(alpha = 0.75f)
    val labelColor = md_onSurfaceVariant
    val pointColor = md_tertiary
    val solutionColor = md_tertiary

    val textMeasurer = rememberTextMeasurer()

    val points = remember(expression, xRange, xCenter) {
        val samples = 300
        val list = ArrayList<Pair<Double, Double>>(samples + 1)
        val xMin = xCenter - xRange
        val xMax = xCenter + xRange
        for (i in 0..samples) {
            val x = xMin + (xMax - xMin) * i / samples
            val y = ExpressionEvaluator.evaluate(expression, x) ?: continue
            if (y.isNaN() || y.isInfinite()) continue
            list.add(x to y)
        }
        list
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(onPan, xRange, yRange) {
                if (onPan == null) return@pointerInput
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = { },
                    onDragCancel = { },
                    onDrag = { change: PointerInputChange, dragAmount: Offset ->
                        change.consume()
                        onPan(
                            dragAmount.x,
                            dragAmount.y,
                            size.width.toFloat(),
                            size.height.toFloat()
                        )
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onCanvasClick() },
                    onDoubleTap = { onDoubleTap() }
                )
            }
    ) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        fun mapX(x: Double): Float =
            ((x - (xCenter - xRange)) / (2.0 * xRange) * w).toFloat()

        fun mapY(y: Double): Float =
            (((yCenter + yRange) - y) / (2.0 * yRange) * h).toFloat()

        val axisY0 = mapY(0.0)
        val axisX0 = mapX(0.0)
        val xAxisVisible = axisY0 in 0f..h
        val yAxisVisible = axisX0 in 0f..w

        if (xAxisVisible) {
            drawLine(axisColor, Offset(0f, axisY0), Offset(w, axisY0), 1.8f)
        }
        if (yAxisVisible) {
            drawLine(axisColor, Offset(axisX0, 0f), Offset(axisX0, h), 1.8f)
        }

        val labelStyle = TextStyle(fontSize = 8.sp, color = labelColor)

        val xTickBaseY = if (xAxisVisible) axisY0 else h - 4f
        run {
            val step = niceStep(2.0 * xRange, 5)
            val start = ceil((xCenter - xRange) / step) * step
            var t = start
            var guard = 0
            while (t <= xCenter + xRange + 1e-9 && guard < 100) {
                val sx = mapX(t)
                if (sx in 0f..w) {
                    drawLine(
                        color = tickColor,
                        start = Offset(sx, xTickBaseY - 5f),
                        end = Offset(sx, xTickBaseY + 5f),
                        strokeWidth = 1.4f
                    )
                    val labelText = formatTick(t)
                    val layout = textMeasurer.measure(text = labelText, style = labelStyle)
                    val labelY = if (xTickBaseY > h - 20f) {
                        xTickBaseY - layout.size.height - 6f
                    } else {
                        xTickBaseY + 6f
                    }
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(sx - layout.size.width / 2f, labelY)
                    )
                }
                t += step
                guard++
            }
        }

        val yTickBaseX = if (yAxisVisible) axisX0 else 4f
        run {
            val step = niceStep(2.0 * yRange, 5)
            val start = ceil((yCenter - yRange) / step) * step
            var t = start
            var guard = 0
            while (t <= yCenter + yRange + 1e-9 && guard < 100) {
                val sy = mapY(t)
                if (sy in 0f..h) {
                    drawLine(
                        color = tickColor,
                        start = Offset(yTickBaseX - 5f, sy),
                        end = Offset(yTickBaseX + 5f, sy),
                        strokeWidth = 1.4f
                    )
                    val labelText = formatTick(t)
                    val layout = textMeasurer.measure(text = labelText, style = labelStyle)
                    val labelX = if (yTickBaseX > w - 30f) {
                        yTickBaseX - layout.size.width - 6f
                    } else {
                        yTickBaseX + 6f
                    }
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(labelX, sy - layout.size.height / 2f)
                    )
                }
                t += step
                guard++
            }
        }

        val path = Path()
        var penDown = false
        var lastY: Double? = null

        points.forEach { pair ->
            val x = pair.first
            val y = pair.second
            if (abs(y - yCenter) > yRange * 2.0) {
                penDown = false
                lastY = null
                return@forEach
            }
            val jump = lastY?.let { abs(y - it) > yRange } ?: false
            if (jump) penDown = false

            val px = mapX(x)
            val py = mapY(y)
            if (!penDown) {
                path.moveTo(px, py)
                penDown = true
            } else {
                path.lineTo(px, py)
            }
            lastY = y
        }
        drawPath(path, lineColor, style = Stroke(width = 2.5f))

        if (selectedX != null) {
            val sx = mapX(selectedX)
            if (sx in 0f..w) {
                drawLine(
                    color = pointColor.copy(alpha = 0.55f),
                    start = Offset(sx, 0f),
                    end = Offset(sx, h),
                    strokeWidth = 1.2f
                )
            }
        }
        if (selectedY != null) {
            val sy = mapY(selectedY)
            if (sy in 0f..h) {
                drawLine(
                    color = pointColor.copy(alpha = 0.55f),
                    start = Offset(0f, sy),
                    end = Offset(w, sy),
                    strokeWidth = 1.2f
                )
            }
        }
        if (selectedX != null && selectedY != null) {
            val sx = mapX(selectedX)
            val sy = mapY(selectedY)
            if (sx in 0f..w && sy in 0f..h) {
                drawCircle(pointColor, radius = 6f, center = Offset(sx, sy))
                drawCircle(Color.White, radius = 2.5f, center = Offset(sx, sy))
            }
        }

        if (targetY != null) {
            val sy = mapY(targetY)
            if (sy in 0f..h) {
                drawLine(
                    color = solutionColor.copy(alpha = 0.55f),
                    start = Offset(0f, sy),
                    end = Offset(w, sy),
                    strokeWidth = 1.2f
                )
            }
            ySolutions.forEach { solX ->
                val sx = mapX(solX)
                if (sx in 0f..w && sy in 0f..h) {
                    drawCircle(
                        color = solutionColor,
                        radius = 7f,
                        center = Offset(sx, sy),
                        style = Stroke(width = 2.5f)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.5f,
                        center = Offset(sx, sy)
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberInputScreen(
    initial: String,
    label: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    BackHandler(enabled = true) { onCancel() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val keypadWidth = screenWidth * 0.72f
        val topOffset = screenHeight * 0.02f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$label ${if (text.isEmpty()) "0" else text}",
                color = md_primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = topOffset, bottom = 8.dp)
            )

            Spacer(Modifier.height(6.dp))

            SimpleNumberPad(
                keypadWidth = keypadWidth,
                onKey = { key ->
                    when (key) {
                        "APPLY" -> if (text.isNotEmpty()) onConfirm(text)
                        "\u232B" -> if (text.isNotEmpty()) text = text.dropLast(1)
                        "CLEAR" -> text = ""
                        else -> text += key
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = calcFunctionBg,
                        contentColor = calcFunctionText
                    )
                ) { Text("取消", fontSize = 12.sp) }

                Button(
                    onClick = { text = "" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = calcFunctionBg,
                        contentColor = calcFunctionText
                    )
                ) { Text("清空", fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun SimpleNumberPad(
    keypadWidth: androidx.compose.ui.unit.Dp,
    onKey: (String) -> Unit
) {
    val rows: List<List<String>> = listOf(
        listOf("7", "8", "9", "\u232B"),
        listOf("4", "5", "6", "."),
        listOf("1", "2", "3", "-"),
        listOf("0", "CLEAR", "APPLY")
    )
    val rowWidths = listOf(1.00f, 1.00f, 1.00f, 0.78f)

    val columnUnit: androidx.compose.ui.unit.Dp = keypadWidth / 4f
    val buttonWidth: androidx.compose.ui.unit.Dp = columnUnit * 0.86f
    val buttonHeight: androidx.compose.ui.unit.Dp = buttonWidth * 0.62f
    val shape = RoundedCornerShape(percent = 50)
    val numberFontSize = (buttonHeight.value * 0.62f).sp
    val smallFontSize = (buttonHeight.value * 0.42f).sp

    Column(
        modifier = Modifier.width(keypadWidth),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEachIndexed { rowIdx, row ->
            Row(
                modifier = Modifier.fillMaxWidth(rowWidths[rowIdx]),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    val (bg, fg) = when (key) {
                        "APPLY" -> calcEqualsBg to calcEqualsText
                        "\u232B", "CLEAR" -> calcFunctionBg to calcFunctionText
                        "-" -> calcOperatorBg to calcOperatorText
                        else -> calcNumberBg to calcNumberText
                    }
                    val label = when (key) {
                        "APPLY" -> "\u2713"
                        "CLEAR" -> "C"
                        else -> key
                    }
                    val fontSize = if (label.length > 1) smallFontSize else numberFontSize

                    Box(
                        modifier = Modifier
                            .width(buttonWidth)
                            .height(buttonHeight)
                            .clip(shape)
                            .background(bg)
                            .clickable { onKey(key) },
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
            }
        }
    }
}

@Composable
private fun ExpressionEditor(
    initial: String,
    scaleFactor: Float,
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
        val keypadWidth = screenWidth * 0.78f * scaleFactor
        val keypadTopOffset = screenHeight * 0.03f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "y = $text",
                color = md_primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
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
                            "APPLY" -> onConfirm(text)
                            "\u232B" -> if (text.isNotEmpty()) {
                                text = text.dropLast(1)
                            }
                            else -> text += key
                        }
                    },
                    // ★ 第一排按钮宽度与计算器一致
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