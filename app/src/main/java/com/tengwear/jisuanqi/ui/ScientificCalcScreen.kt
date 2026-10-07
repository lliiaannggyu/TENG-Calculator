@file:Suppress("SpellCheckingInspection")

package com.tengwear.jisuanqi.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.tengwear.jisuanqi.logic.ScientificCalcViewModel
import com.tengwear.jisuanqi.ui.theme.*
import java.util.Locale
import kotlin.math.abs

private const val SCI_PAGE_COUNT = 4

// ============ 4 页键盘 ============

/**
 * 第 1 页 · 数字与基本运算（最常用，打开就能用）
 *
 *  7   8   9   ⌫   ÷   ×
 *  4   5   6   (   )   −
 *  1   2   3   .   0   +
 *  Ans xʸ  %   π   e   =
 */
val SciCalcKeyboard1: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("7", "7"),
        KeyDef("8", "8"),
        KeyDef("9", "9"),
        KeyDef("\u232B", "\u232B"),
        KeyDef("/", Symbols.DIVIDE),
        KeyDef("*", Symbols.TIMES)
    ),
    listOf(
        KeyDef("4", "4"),
        KeyDef("5", "5"),
        KeyDef("6", "6"),
        KeyDef("(", "("),
        KeyDef(")", ")"),
        KeyDef("-", Symbols.MINUS)
    ),
    listOf(
        KeyDef("1", "1"),
        KeyDef("2", "2"),
        KeyDef("3", "3"),
        KeyDef(".", "."),
        KeyDef("0", "0"),
        KeyDef("+", "+")
    ),
    listOf(
        KeyDef("Ans", "Ans"),
        KeyDef("pow", "x" + Symbols.SUP_Y),
        KeyDef("%", "%"),
        KeyDef("pi", "\u03C0"),
        KeyDef("e", "e"),
        KeyDef("=", "=")
    )
)

/**
 * 第 2 页 · 三角与对数
 *
 *  sin    cos    tan    sin⁻¹  cos⁻¹  tan⁻¹
 *  sinh   cosh   tanh   ln     log    log₂
 *  √      ∛      x²     x³     abs    1/x
 *  (      )      Ans    ⌫
 */
val SciCalcKeyboard2: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("sin",  "sin"),
        KeyDef("cos",  "cos"),
        KeyDef("tan",  "tan"),
        KeyDef("asin", "sin\u207B\u00B9"),
        KeyDef("acos", "cos\u207B\u00B9"),
        KeyDef("atan", "tan\u207B\u00B9")
    ),
    listOf(
        KeyDef("sinh", "sinh"),
        KeyDef("cosh", "cosh"),
        KeyDef("tanh", "tanh"),
        KeyDef("ln",   "ln"),
        KeyDef("log",  "log"),
        KeyDef("log2", "log\u2082")
    ),
    listOf(
        KeyDef("sqrt", Symbols.SQRT),
        KeyDef("cbrt", "\u221B"),
        KeyDef("sq",   "x\u00B2"),
        KeyDef("cube", "x\u00B3"),
        KeyDef("abs",  "abs"),
        KeyDef("recip", "1/x")
    ),
    listOf(
        KeyDef("(", "("),
        KeyDef(")", ")"),
        KeyDef("Ans", "Ans"),
        KeyDef("\u232B", "\u232B")
    )
)

/**
 * 第 3 页 · 进阶函数与内存
 *
 *  n!    10ʸ   2ʸ    eʸ    rnd   DMS
 *  ⌊x⌋   ⌈x⌉   EXP   M+    M−    MR
 *  MC    sinh⁻¹ cosh⁻¹ tanh⁻¹
 *  (     )     Ans   ⌫
 */
val SciCalcKeyboard3: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("fact",  "n!"),
        KeyDef("10pow", "10\u02B8"),
        KeyDef("2pow",  "2\u02B8"),
        KeyDef("epow",  "e\u02B8"),
        KeyDef("rnd",   "rnd"),
        KeyDef("DMS",   "DMS")
    ),
    listOf(
        KeyDef("floor", "\u230Ax\u230B"),
        KeyDef("ceil",  "\u2308x\u2309"),
        KeyDef("exp",   "EXP"),
        KeyDef("M+",    "M+"),
        KeyDef("M-",    "M\u2212"),
        KeyDef("MR",    "MR")
    ),
    listOf(
        KeyDef("MC",    "MC"),
        KeyDef("asinh", "sinh\u207B\u00B9"),
        KeyDef("acosh", "cosh\u207B\u00B9"),
        KeyDef("atanh", "tanh\u207B\u00B9")
    ),
    listOf(
        KeyDef("(", "("),
        KeyDef(")", ")"),
        KeyDef("Ans", "Ans"),
        KeyDef("\u232B", "\u232B")
    )
)

/**
 * 第 4 页 · 物理常量
 *
 *  c    h    k    Nₐ   R    G
 *  g    ε₀   μ₀   mₑ   mₚ   Mᴇ
 *  φ    γ    τ    ln2  ln10 log₂e
 *  (    )    e    π
 */
val SciCalcKeyboard4: List<List<KeyDef>> = listOf(
    listOf(
        KeyDef("const_c", "c"),
        KeyDef("const_h", "h"),
        KeyDef("const_k", "k"),
        KeyDef("const_Na", "N\u2090"),
        KeyDef("const_R", "R"),
        KeyDef("const_G", "G")
    ),
    listOf(
        KeyDef("const_g", "g"),
        KeyDef("const_eps0", "\u03B5\u2080"),
        KeyDef("const_mu0", "\u03BC\u2080"),
        KeyDef("const_me", "m\u2091"),
        KeyDef("const_mp", "m\u209A"),
        KeyDef("const_Me", "M\u2091")
    ),
    listOf(
        KeyDef("const_phi", "\u03C6"),
        KeyDef("const_gam", "\u03B3"),
        KeyDef("const_tau", "\u03C4"),
        KeyDef("const_ln2", "ln2"),
        KeyDef("const_ln10", "ln10"),
        KeyDef("const_log2e", "log\u2082e")
    ),
    listOf(
        KeyDef("(", "("),
        KeyDef(")", ")"),
        KeyDef("e", "e"),
        KeyDef("pi", "\u03C0")
    )
)

// ============ 行宽 ============

val SciCalcRowWidths1: List<Float> = listOf(1.00f, 0.96f, 0.82f, 0.62f)
val SciCalcRowWidths2: List<Float> = listOf(1.00f, 0.96f, 0.82f, 0.62f)
val SciCalcRowWidths3: List<Float> = listOf(1.00f, 1.00f, 0.80f, 0.62f)
val SciCalcRowWidths4: List<Float> = listOf(1.00f, 0.96f, 0.82f, 0.62f)

// ============ 界面 ============

@Composable
fun ScientificCalcScreen(
    scaleFactor: Float,
    onDismiss: () -> Unit
) {
    val viewModel: ScientificCalcViewModel = viewModel()
    val state = viewModel.state

    val keyboards = remember {
        listOf(SciCalcKeyboard1, SciCalcKeyboard2, SciCalcKeyboard3, SciCalcKeyboard4)
    }
    val rowWidthsList = remember {
        listOf(SciCalcRowWidths1, SciCalcRowWidths2, SciCalcRowWidths3, SciCalcRowWidths4)
    }
    var keyboardIndex by remember { mutableIntStateOf(0) }

    val config = LocalConfiguration.current
    val screenWidth: Dp = config.screenWidthDp.dp
    val screenHeight: Dp = config.screenHeightDp.dp

    AppScaffold {
        ScreenScaffold(timeText = { TimeText() }) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        var totalDx = 0f
                        var totalDy = 0f
                        detectDragGestures(
                            onDragStart = { totalDx = 0f; totalDy = 0f },
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
                            onDragCancel = { totalDx = 0f; totalDy = 0f },
                            onDrag = { _, dragAmount ->
                                totalDx += dragAmount.x
                                totalDy += dragAmount.y
                            }
                        )
                    }
            ) {
                val keypadWidth: Dp = screenWidth * 0.80f * scaleFactor

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SciDisplayArea(
                        expression = state.expression,
                        display = state.display,
                        screenWidth = screenWidth,
                        angleModeLabel = viewModel.angleMode.label,
                        showMemory = viewModel.hasMemory,
                        memoryValue = viewModel.memory,
                        onToggleAngleMode = { viewModel.toggleAngleMode() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.15f)
                    )

                    SciPageIndicator(
                        current = keyboardIndex,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.9f),
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
                            label = "sci_kb"
                        ) { idx ->
                            CalcKeyboardView(
                                rows = keyboards[idx],
                                rowWidths = rowWidthsList[idx],
                                keypadWidth = keypadWidth,
                                onKeyClick = { handleSciKey(it, viewModel) },
                                firstRowScale = 0.88f,
                                hapticEnabled = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SciPageIndicator(current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until SCI_PAGE_COUNT) {
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

@Composable
private fun SciDisplayArea(
    expression: String,
    display: String,
    screenWidth: Dp,
    angleModeLabel: String,
    showMemory: Boolean,
    memoryValue: Double,
    onToggleAngleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(calcFunctionBg)
                    .clickable { onToggleAngleMode() }
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = angleModeLabel,
                    color = calcFunctionText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(6.dp))
            if (showMemory) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(calcOperatorBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "M ${String.format(Locale.US, "%.4g", memoryValue)}",
                        color = calcOperatorText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.weight(1f))
        }

        if (expression.isNotEmpty()) {
            Text(
                text = expression,
                color = calcExpressionText,
                fontSize = (screenWidth.value * 0.042f).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        val displayFontSize: TextUnit = when {
            display.length <= 5 -> (screenWidth.value * 0.125f).sp
            display.length <= 8 -> (screenWidth.value * 0.100f).sp
            display.length <= 12 -> (screenWidth.value * 0.084f).sp
            else -> (screenWidth.value * 0.068f).sp
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = display,
                color = calcDisplayText,
                fontSize = displayFontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun handleSciKey(key: String, viewModel: ScientificCalcViewModel) {
    when (key) {
        "AC" -> viewModel.onBackspace()
        "+/-" -> viewModel.onToggleSign()
        "+", "-", "*", "/" -> viewModel.onOperator(key)
        "=" -> viewModel.onEquals()
        "pow" -> viewModel.onOperator("pow")
        "sin", "cos", "tan", "asin", "acos", "atan",
        "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
        "ln", "log", "log2", "sqrt", "cbrt",
        "sq", "cube", "10pow", "2pow", "epow",
        "abs", "recip", "fact", "floor", "ceil",
        "%" -> viewModel.onScientificUnary(key)
        "e" -> viewModel.onConstantE()
        "pi" -> viewModel.onConstantPi()
        else -> {
            when {
                key.startsWith("const_") -> viewModel.onConstantKey(key)
                key == "exp" || key == "EXP" -> viewModel.onExp()
                key == "DMS" -> viewModel.onDms()
                key == "M+" -> viewModel.memoryAdd()
                key == "M-" -> viewModel.memorySubtract()
                key == "MR" -> viewModel.memoryRecall()
                key == "MC" -> viewModel.memoryClear()
                key == "rnd" -> viewModel.onRandom()
                key == "Ans" -> viewModel.onAnswer()
                key == "(" -> viewModel.onInput("(")
                key == ")" -> viewModel.onInput(")")
                key == "\u232B" -> viewModel.onBackspace()
                else -> viewModel.onInput(key)
            }
        }
    }
}