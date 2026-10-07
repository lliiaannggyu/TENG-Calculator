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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.tengwear.jisuanqi.logic.ExpressionEvaluator
import com.tengwear.jisuanqi.logic.PolynomialSolver
import com.tengwear.jisuanqi.ui.theme.*
import kotlin.math.abs

private enum class EquationMode { EDIT, RESULT }

private fun splitEquation(input: String): Pair<String, String>? {
    val idx = input.indexOf('=')
    if (idx < 0) return null
    val lhs = input.substring(0, idx).trim()
    val rhs = input.substring(idx + 1).trim()
    if (lhs.isEmpty() || rhs.isEmpty()) return null
    return lhs to rhs
}

private fun makeF(input: String): ((Double) -> Double?)? {
    val parts = splitEquation(input) ?: return null
    val lhs = parts.first
    val rhs = parts.second
    return { x ->
        val l = ExpressionEvaluator.evaluate(lhs, x)
        val r = ExpressionEvaluator.evaluate(rhs, x)
        if (l == null || r == null) null else l - r
    }
}

@Composable
fun EquationSolverScreen(onDismiss: () -> Unit) {
    var mode by remember { mutableStateOf(EquationMode.EDIT) }
    var equation by remember { mutableStateOf("") }
    var steps by remember { mutableStateOf<List<String>>(emptyList()) }
    var solutions by remember { mutableStateOf<List<Double>>(emptyList()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

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
                    EquationMode.EDIT -> EquationEditor(
                        initial = equation,
                        onConfirm = { input ->
                            equation = input
                            errorMsg = null
                            steps = emptyList()
                            solutions = emptyList()

                            if (!input.contains("=")) {
                                errorMsg = "缺少等号"
                            } else if (!input.contains("x")) {
                                errorMsg = "缺少变量 x"
                            } else {
                                val parts = splitEquation(input)
                                if (parts == null) {
                                    errorMsg = "表达式无效"
                                } else {
                                    val (lhs, rhs) = parts
                                    val fExpr = "($lhs)-($rhs)"
                                    val poly = PolynomialSolver.fit(fExpr)

                                    if (poly != null) {
                                        steps = PolynomialSolver.steps(poly)
                                        solutions = PolynomialSolver.solve(poly)
                                        if (solutions.isEmpty() &&
                                            steps.none { it.contains("无实根") }
                                        ) {
                                            errorMsg = "无解"
                                        }
                                    } else {
                                        val f = makeF(input)
                                        if (f == null) {
                                            errorMsg = "表达式无效"
                                        } else {
                                            solutions = PolynomialSolver.scanRoots(
                                                f, -100.0, 100.0
                                            )
                                            steps = listOf(
                                                "非多项式方程",
                                                "数值法扫描 [-100, 100]"
                                            )
                                            if (solutions.isEmpty()) {
                                                val testX = listOf(-1.234, 0.567, 1.789)
                                                val allZero = testX.all { tx ->
                                                    val v = f(tx)
                                                    v != null && abs(v) < 1e-6
                                                }
                                                errorMsg = if (allZero) {
                                                    "恒等式（无限多解）"
                                                } else {
                                                    "无解"
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            mode = EquationMode.RESULT
                        },
                        onCancel = {
                            if (equation.isEmpty()) onDismiss()
                            else mode = EquationMode.RESULT
                        }
                    )

                    EquationMode.RESULT -> EquationResult(
                        equation = equation,
                        steps = steps,
                        solutions = solutions,
                        errorMsg = errorMsg,
                        onEdit = { mode = EquationMode.EDIT },
                        onDismiss = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun EquationResult(
    equation: String,
    steps: List<String>,
    solutions: List<Double>,
    errorMsg: String?,
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
            text = "方程求解",
            color = md_primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(2.dp))

        Text(
            text = prettyExpression(equation),
            color = md_onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
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
                items(steps) { line ->
                    Text(
                        text = line,
                        color = md_onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (errorMsg != null) {
                    item {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = errorMsg,
                            color = md_tertiary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (solutions.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(4.dp))
                    }
                    itemsIndexed(solutions) { idx, v ->
                        val label = if (solutions.size > 1) {
                            val sub = when (idx) {
                                0 -> "\u2081"
                                1 -> "\u2082"
                                2 -> "\u2083"
                                3 -> "\u2084"
                                4 -> "\u2085"
                                else -> "\u2086"
                            }
                            "x$sub = ${PolynomialSolver.num(v)}"
                        } else {
                            "x = ${PolynomialSolver.num(v)}"
                        }
                        Text(
                            text = label,
                            color = md_tertiary,
                            fontSize = 14.sp,
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
            RoundIconButton(label = "编辑") { onEdit() }
            RoundIconButton(label = "返回") { onDismiss() }
        }
    }
}

/**
 * 编辑页 — 键盘宽度 1.0f（等于屏幕宽度）
 */
@Composable
private fun EquationEditor(
    initial: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit
) {
    var text by remember { mutableStateOf(initial) }

    val keyboards = remember { listOf(EquationKeyboardBasic, EquationKeyboardFunc) }
    val rowWidthsList = remember { listOf(EquationRowWidthsBasic, EquationRowWidthsFunc) }
    var keyboardIndex by remember { mutableIntStateOf(0) }

    BackHandler(enabled = true) { onCancel() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        // ★ 键盘宽度：1.2f → 1.0f
        val keypadWidth = screenWidth * 1.0f
        val keypadTopOffset = screenHeight * 0.02f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (text.isEmpty()) "输入方程…" else prettyExpression(text),
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
                    }
                )
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