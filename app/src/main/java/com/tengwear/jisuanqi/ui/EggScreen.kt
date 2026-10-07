@file:Suppress("SpellCheckingInspection")

package com.tengwear.jisuanqi.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.tengwear.jisuanqi.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

private const val PHASE_IDLE = 0
private const val PHASE_GATHER = 1
private const val PHASE_SPIN = 2
private const val PHASE_FLY = 3

private const val GATHER_TIME = 0.35f
private const val GATHER_RADIUS = 30f
private const val SPIN_ACCEL_TIME = 0.5f
private const val PEAK_OMEGA = 22f
private const val FLY_FRICTION = 0.35f

private const val LETTER_COUNT = 4
private const val PHASE_STEP = (2f * PI / LETTER_COUNT).toFloat()

@Composable
fun EggScreen(
    variant: Int,
    onDismiss: () -> Unit
) {
    @Suppress("UNUSED_EXPRESSION")
    onDismiss

    // ===== teng（小写）走"假闪退"彩蛋分支 =====
    if (variant == 1) {
        FakeCrashEgg()
        return
    }

    // ===== TENG（大写）走原来的拖动 + 聚拢彩蛋 =====
    val word = "TENG"
    val letters = word.map { it.toString() }

    AppScaffold {
        ScreenScaffold(
            timeText = { TimeText() }
        ) { paddingValues ->
            val config = LocalConfiguration.current
            val density = config.densityDpi / 160f
            val canvasW = (config.screenWidthDp * density).coerceAtLeast(100f)
            val canvasH = (config.screenHeightDp * density).coerceAtLeast(100f)

            val letterSizeDp = 44.dp
            val letterSizePx = letterSizeDp.value * density

            val gap = letterSizePx * 0.72f
            val totalW = (letters.size - 1) * gap
            val startX = canvasW / 2f - totalW / 2f - letterSizePx / 2f

            val posX = remember {
                List(letters.size) { i -> mutableFloatStateOf(startX + i * gap) }
            }
            val posY = remember {
                List(letters.size) { mutableFloatStateOf(canvasH / 2f - letterSizePx / 2f) }
            }
            val velX = remember { List(letters.size) { mutableFloatStateOf(0f) } }
            val velY = remember { List(letters.size) { mutableFloatStateOf(0f) } }

            val gatherCenterX = remember { mutableFloatStateOf(canvasW / 2f) }
            val gatherCenterY = remember { mutableFloatStateOf(canvasH / 2f) }
            val gatherTargetX = remember { List(letters.size) { mutableFloatStateOf(0f) } }
            val gatherTargetY = remember { List(letters.size) { mutableFloatStateOf(0f) } }
            val gatherStartX = remember { List(letters.size) { mutableFloatStateOf(0f) } }
            val gatherStartY = remember { List(letters.size) { mutableFloatStateOf(0f) } }

            val spinAngle = remember {
                List(letters.size) { i -> mutableFloatStateOf(i * PHASE_STEP) }
            }
            val spinRadius = remember { List(letters.size) { mutableFloatStateOf(GATHER_RADIUS) } }
            val spinCenterX = remember { mutableFloatStateOf(canvasW / 2f) }
            val spinCenterY = remember { mutableFloatStateOf(canvasH / 2f) }

            var phase by remember { mutableIntStateOf(PHASE_IDLE) }
            var phaseTime by remember { mutableFloatStateOf(0f) }
            var omega by remember { mutableFloatStateOf(0f) }

            LaunchedEffect(Unit) {
                var lastT = 0L
                while (true) {
                    withFrameNanos { now ->
                        if (lastT == 0L) {
                            lastT = now
                        } else {
                            val dt = ((now - lastT) / 1_000_000_000f).coerceIn(0f, 0.05f)
                            lastT = now

                            when (phase) {
                                PHASE_IDLE -> {
                                    for (i in letters.indices) {
                                        val vx = velX[i].floatValue
                                        val vy = velY[i].floatValue
                                        if (vx != 0f || vy != 0f) {
                                            posX[i].floatValue =
                                                (posX[i].floatValue + vx * dt)
                                                    .coerceIn(0f, canvasW - letterSizePx)
                                            posY[i].floatValue =
                                                (posY[i].floatValue + vy * dt)
                                                    .coerceIn(0f, canvasH - letterSizePx)
                                            val f = FLY_FRICTION.pow(dt)
                                            velX[i].floatValue = vx * f
                                            velY[i].floatValue = vy * f
                                            if (abs(velX[i].floatValue) < 2f) velX[i].floatValue = 0f
                                            if (abs(velY[i].floatValue) < 2f) velY[i].floatValue = 0f
                                        }
                                    }
                                }

                                PHASE_GATHER -> {
                                    phaseTime += dt
                                    val t = (phaseTime / GATHER_TIME).coerceIn(0f, 1f)
                                    val ease = 1f - (1f - t) * (1f - t)

                                    for (i in letters.indices) {
                                        val sx = gatherStartX[i].floatValue
                                        val sy = gatherStartY[i].floatValue
                                        val tx = gatherTargetX[i].floatValue
                                        val ty = gatherTargetY[i].floatValue
                                        posX[i].floatValue = sx + (tx - sx) * ease
                                        posY[i].floatValue = sy + (ty - sy) * ease
                                    }

                                    if (phaseTime >= GATHER_TIME) {
                                        phase = PHASE_SPIN
                                        phaseTime = 0f
                                        omega = 0f
                                        spinCenterX.floatValue = gatherCenterX.floatValue
                                        spinCenterY.floatValue = gatherCenterY.floatValue
                                        for (i in letters.indices) {
                                            spinAngle[i].floatValue = i * PHASE_STEP
                                            spinRadius[i].floatValue = GATHER_RADIUS
                                        }
                                    }
                                }

                                PHASE_SPIN -> {
                                    phaseTime += dt
                                    omega = (PEAK_OMEGA * (phaseTime / SPIN_ACCEL_TIME))
                                        .coerceAtMost(PEAK_OMEGA)

                                    val cx0 = spinCenterX.floatValue
                                    val cy0 = spinCenterY.floatValue
                                    for (i in letters.indices) {
                                        spinAngle[i].floatValue += omega * dt
                                        val a = spinAngle[i].floatValue
                                        val r = spinRadius[i].floatValue
                                        val cx = cx0 + r * cos(a)
                                        val cy = cy0 + r * sin(a)
                                        posX[i].floatValue =
                                            (cx - letterSizePx / 2f).coerceIn(0f, canvasW - letterSizePx)
                                        posY[i].floatValue =
                                            (cy - letterSizePx / 2f).coerceIn(0f, canvasH - letterSizePx)
                                    }
                                }

                                PHASE_FLY -> {
                                    for (i in letters.indices) {
                                        val vx = velX[i].floatValue
                                        val vy = velY[i].floatValue
                                        posX[i].floatValue =
                                            (posX[i].floatValue + vx * dt)
                                                .coerceIn(0f, canvasW - letterSizePx)
                                        posY[i].floatValue =
                                            (posY[i].floatValue + vy * dt)
                                                .coerceIn(0f, canvasH - letterSizePx)
                                        val f = FLY_FRICTION.pow(dt)
                                        velX[i].floatValue = vx * f
                                        velY[i].floatValue = vy * f
                                    }
                                    if (letters.indices.all {
                                            abs(velX[it].floatValue) < 2f &&
                                                    abs(velY[it].floatValue) < 2f
                                        }) {
                                        phase = PHASE_IDLE
                                        for (i in letters.indices) {
                                            velX[i].floatValue = 0f
                                            velY[i].floatValue = 0f
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                letters.forEachIndexed { i, letter ->
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    posX[i].floatValue.roundToInt(),
                                    posY[i].floatValue.roundToInt()
                                )
                            }
                            .size(letterSizeDp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            color = md_primary,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                val downX = down.position.x
                                val downY = down.position.y

                                var draggedIndex = -1
                                for (i in letters.indices) {
                                    val lx = posX[i].floatValue
                                    val ly = posY[i].floatValue
                                    if (downX >= lx && downX <= lx + letterSizePx &&
                                        downY >= ly && downY <= ly + letterSizePx
                                    ) {
                                        draggedIndex = i
                                        break
                                    }
                                }

                                if (draggedIndex >= 0 && phase == PHASE_IDLE) {
                                    var lastX = downX
                                    var lastY = downY
                                    var lastTime = System.currentTimeMillis()

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id }
                                            ?: break
                                        if (!change.pressed) break

                                        val curX = change.position.x
                                        val curY = change.position.y
                                        val dx = curX - lastX
                                        val dy = curY - lastY

                                        posX[draggedIndex].floatValue =
                                            (posX[draggedIndex].floatValue + dx)
                                                .coerceIn(0f, canvasW - letterSizePx)
                                        posY[draggedIndex].floatValue =
                                            (posY[draggedIndex].floatValue + dy)
                                                .coerceIn(0f, canvasH - letterSizePx)

                                        val nowMs = System.currentTimeMillis()
                                        val dtMs = (nowMs - lastTime).coerceAtLeast(1)
                                        velX[draggedIndex].floatValue = dx / dtMs * 1000f
                                        velY[draggedIndex].floatValue = dy / dtMs * 1000f

                                        lastX = curX
                                        lastY = curY
                                        lastTime = nowMs
                                    }
                                } else {
                                    if (phase != PHASE_GATHER && phase != PHASE_SPIN) {
                                        gatherCenterX.floatValue = downX
                                        gatherCenterY.floatValue = downY

                                        for (i in letters.indices) {
                                            val a = i * PHASE_STEP
                                            gatherTargetX[i].floatValue =
                                                downX - letterSizePx / 2f + GATHER_RADIUS * cos(a)
                                            gatherTargetY[i].floatValue =
                                                downY - letterSizePx / 2f + GATHER_RADIUS * sin(a)
                                            gatherStartX[i].floatValue = posX[i].floatValue
                                            gatherStartY[i].floatValue = posY[i].floatValue
                                            velX[i].floatValue = 0f
                                            velY[i].floatValue = 0f
                                        }
                                        phaseTime = 0f
                                        phase = PHASE_GATHER
                                    }

                                    while (true) {
                                        val event = awaitPointerEvent()
                                        val change = event.changes.firstOrNull { it.id == down.id }
                                            ?: break
                                        if (!change.pressed) break
                                    }

                                    if (phase == PHASE_SPIN) {
                                        val currentOmega = omega
                                        for (i in letters.indices) {
                                            val a = spinAngle[i].floatValue
                                            val r = spinRadius[i].floatValue
                                            val tangentSpeed = currentOmega * r
                                            velX[i].floatValue = -sin(a) * tangentSpeed
                                            velY[i].floatValue = cos(a) * tangentSpeed
                                        }
                                        phase = PHASE_FLY
                                    } else if (phase == PHASE_GATHER) {
                                        phase = PHASE_IDLE
                                    }
                                }
                            }
                        }
                )
            }
        }
    }
}

/**
 * teng（小写）的假闪退彩蛋
 *
 * 点击屏幕 → 显示"这是彩蛋" → 800ms 后主动抛异常 → 系统弹出"应用已停止"
 */
@Composable
private fun FakeCrashEgg() {
    var textShown by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AppScaffold {
        ScreenScaffold(
            timeText = { Text("") }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(md_background),
                contentAlignment = Alignment.Center
            ) {
                if (!textShown) {
                    // 点击屏幕后触发假闪退
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                textShown = true
                                scope.launch {
                                    delay(800)
                                    // ★ 主动抛异常 → 系统崩溃
                                    throw RuntimeException("这是彩蛋")
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "teng",
                            color = md_primary,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "这是彩蛋",
                        color = md_primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}