@file:Suppress("SpellCheckingInspection")

package com.tengwear.jisuanqi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
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
import com.tengwear.jisuanqi.data.FormulaTemplate
import com.tengwear.jisuanqi.logic.ExpressionEvaluator
import com.tengwear.jisuanqi.logic.TemplateVars
import com.tengwear.jisuanqi.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

// ============================ 公式键盘布局 ============================

private val LETTER_ROWS = listOf(
    listOf("a", "b", "c", "d", "e"),
    listOf("f", "g", "h", "i", "j"),
    listOf("k", "l", "m", "n", "o"),
    listOf("p", "q", "r", "s", "t"),
    listOf("u", "v", "w", "x", "y"),
    listOf("z", "⌫", "C", "", "")
)

private val NUMBER_ROWS = listOf(
    listOf("7", "8", "9", "(", ")"),
    listOf("4", "5", "6", "+", "-"),
    listOf("1", "2", "3", "*", "/"),
    listOf("0", ".", "^", "%", "e"),
    listOf("π", ",", "⌫", "C", "")
)

private val FUNCTION_ROWS = listOf(
    listOf("sin", "cos", "tan", "asin", "acos"),
    listOf("atan", "ln", "log", "sqrt", "abs"),
    listOf("π", "e", "^", "(", ")"),
    listOf("+", "-", "*", "/", "."),
    listOf(",", "⌫", "C", "", "")
)

private val KEYPAD_TABS = listOf("abc", "123", "f(x)")

// ============================ 编辑页 ============================

@Composable
fun TemplateEditScreen(
    template: FormulaTemplate?,
    onSave: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    val isNew = template == null
    var name by remember { mutableStateOf(template?.name ?: "") }
    var formulaText by remember { mutableStateOf(template?.formula ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var keyboardPage by remember { mutableIntStateOf(0) }

    val variables: List<String>? = remember(formulaText) {
        TemplateVars.extract(formulaText)
    }

    val nameFocus = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    BackHandler(enabled = true) { onCancel() }

    LaunchedEffect(Unit) {
        if (isNew) {
            delay(150L)
            nameFocus.requestFocus()
            keyboard?.show()
        }
    }

    fun handleFormulaKey(key: String) {
        when (key) {
            "" -> Unit
            "⌫" -> if (formulaText.isNotEmpty()) formulaText = formulaText.dropLast(1)
            "C" -> formulaText = ""
            else -> {
                val insert = when (key) {
                    "π" -> "pi"
                    "sin", "cos", "tan", "asin", "acos", "atan",
                    "ln", "log", "sqrt", "abs" -> "$key("
                    else -> key
                }
                formulaText += insert
            }
        }
    }

    AppScaffold {
        ScreenScaffold(
            timeText = { TimeText() }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isNew) "新建模板" else "编辑模板",
                    color = md_primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 4.dp)
                )

                FieldLabel("名称")
                InputBox {
                    if (name.isEmpty()) {
                        Text(
                            text = "例如：二次函数求值",
                            color = md_onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocus),
                        textStyle = TextStyle(color = calcDisplayText, fontSize = 13.sp),
                        cursorBrush = SolidColor(md_primary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboard?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    )
                }

                Spacer(Modifier.height(6.dp))

                FieldLabel("公式")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(calcFunctionBg)
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    if (formulaText.isEmpty()) {
                        Text(
                            text = "点下方键盘输入公式",
                            color = md_onSurfaceVariant.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    } else {
                        Text(
                            text = formulaText + "|",
                            color = calcDisplayText,
                            fontSize = 13.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                val hint = when {
                    variables == null -> "⚠ 字母变量之间请用 * 分隔，如 a*b"
                    variables.isEmpty() -> "a-z 都是未知数；函数需加括号如 sin(x)"
                    else -> "变量：${variables.joinToString(", ")}"
                }
                val hintColor = when {
                    variables == null -> md_error
                    variables.isEmpty() -> md_onSurfaceVariant
                    else -> md_secondary
                }
                Text(
                    text = hint,
                    color = hintColor,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = error!!,
                        color = md_error,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(4.dp))

                FormulaKeypad(
                    page = keyboardPage,
                    onPageChange = { keyboardPage = it },
                    onKey = ::handleFormulaKey
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onCancel() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = calcFunctionBg,
                            contentColor = calcFunctionText
                        )
                    ) {
                        Text("返回", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Button(
                        onClick = {
                            val n = name.trim()
                            val f = formulaText.trim()
                            when {
                                n.isEmpty() -> error = "名称不能为空"
                                f.isEmpty() -> error = "公式不能为空"
                                else -> {
                                    val err = TemplateVars.validate(f)
                                    if (err != null) error = err
                                    else onSave(n, f)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = calcEqualsBg,
                            contentColor = calcEqualsText
                        )
                    ) {
                        Text("保存", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ============================ 公式键盘组件 ============================

@Composable
private fun FormulaKeypad(
    page: Int,
    onPageChange: (Int) -> Unit,
    onKey: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KEYPAD_TABS.forEachIndexed { i, label ->
                val selected = i == page
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) md_primaryContainer else calcFunctionBg)
                        .clickable { onPageChange(i) }
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (selected) md_onPrimaryContainer else calcFunctionText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        val rows = when (page) {
            0 -> LETTER_ROWS
            1 -> NUMBER_ROWS
            else -> FUNCTION_ROWS
        }

        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (key in row) {
                    if (key.isEmpty()) {
                        Spacer(
                            Modifier
                                .weight(1f)
                                .height(26.dp)
                        )
                    } else {
                        FormulaKey(key, Modifier.weight(1f)) { onKey(key) }
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
        }
    }
}

@Composable
private fun FormulaKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val (bg, fg) = when (label) {
        "C" -> md_error.copy(alpha = 0.22f) to md_error
        "⌫" -> calcOperatorBg to calcOperatorText
        else -> calcFunctionBg to calcFunctionText
    }
    Box(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

// ============================ 编辑页小组件 ============================

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = md_secondary,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun InputBox(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(calcFunctionBg)
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        content()
    }
}

// ============================ 计算页 ============================

private sealed interface CalcState {
    object Waiting : CalcState
    data class Success(val value: Double) : CalcState
    data class Failure(val message: String) : CalcState
}

@Composable
fun TemplateCalcScreen(
    template: FormulaTemplate,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val variables: List<String> = remember(template.formula) {
        TemplateVars.extract(template.formula).orEmpty()
    }
    val values = remember(template.id, variables) {
        mutableStateMapOf<String, String>().apply {
            variables.forEach { put(it, "") }
        }
    }

    // 当前激活的变量 + 键盘是否显示
    var activeVar by remember(template.id, variables) {
        mutableStateOf<String?>(null)
    }
    var showKeypad by remember(template.id) { mutableStateOf(false) }

    val state by remember {
        derivedStateOf {
            if (variables.isEmpty()) {
                return@derivedStateOf CalcState.Failure("模板没有可用变量")
            }
            val filled = variables.all { (values[it] ?: "").isNotBlank() }
            if (!filled) return@derivedStateOf CalcState.Waiting

            val map = mutableMapOf<String, Double>()
            for (v in variables) {
                val d = values[v]?.trim()?.toDoubleOrNull()
                    ?: return@derivedStateOf CalcState.Failure("变量 $v 的值无效")
                map[v] = d
            }
            val result = ExpressionEvaluator.evaluateVars(template.formula, map)
                ?: return@derivedStateOf CalcState.Failure("计算失败，请检查输入")
            CalcState.Success(result)
        }
    }

    // 返回键：先收键盘，再退页面
    BackHandler(enabled = true) {
        if (showKeypad) {
            showKeypad = false
            activeVar = null
        } else {
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ===== 头部（固定）=====
        Text(
            text = template.name,
            color = md_primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = template.formula,
            color = md_onSurfaceVariant,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.dp)
        )

        Spacer(Modifier.height(4.dp))

        // ===== 变量 + 结果（滚动区）=====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            for (v in variables) {
                VarInputRow(
                    name = v,
                    value = values[v] ?: "",
                    isActive = showKeypad && v == activeVar,
                    onClick = {
                        if (showKeypad && activeVar == v) {
                            // 再点一次同样的输入框 → 收起键盘
                            showKeypad = false
                            activeVar = null
                        } else {
                            activeVar = v
                            showKeypad = true
                        }
                    }
                )
                Spacer(Modifier.height(4.dp))
            }
            Spacer(Modifier.height(4.dp))
            ResultBox(state)
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(4.dp))

        // ===== 数字键盘（只在点输入框后显示）=====
        if (showKeypad && activeVar != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "正在输入：$activeVar",
                    color = md_secondary,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(calcFunctionBg)
                        .clickable {
                            showKeypad = false
                            activeVar = null
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "收起",
                        color = calcFunctionText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            NumberKeypad(
                onKey = { key ->
                    val v = activeVar ?: return@NumberKeypad
                    val current = values[v] ?: ""
                    when (key) {
                        "" -> Unit
                        "⌫" -> values[v] = if (current.isEmpty()) "" else current.dropLast(1)
                        "C" -> values[v] = ""
                        else -> values[v] = current + key
                    }
                }
            )
            Spacer(Modifier.height(4.dp))
        }

        // ===== 底部按钮 =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MiniButton("编辑", calcFunctionBg, calcFunctionText, Modifier.weight(1f)) {
                onEdit()
            }
            MiniButton("返回", calcEqualsBg, calcEqualsText, Modifier.weight(1f)) {
                onBack()
            }
        }
    }
}

// ============================ 变量输入行 ============================

@Composable
private fun VarInputRow(
    name: String,
    value: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .clip(CircleShape)
                .background(calcOperatorBg)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name,
                color = calcOperatorText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.width(6.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(calcFunctionBg)
                .padding(horizontal = 10.dp, vertical = 7.dp)
        ) {
            val showPlaceholder = value.isEmpty() && !isActive
            val text = when {
                showPlaceholder -> "点此输入"
                isActive -> value + "|"
                else -> value
            }
            Text(
                text = text,
                color = if (showPlaceholder) md_onSurfaceVariant.copy(alpha = 0.5f)
                else calcDisplayText,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================ 数字键盘组件 ============================

@Composable
private fun NumberKeypad(onKey: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        KeypadRow(listOf("7", "8", "9", "⌫"), onKey)
        Spacer(Modifier.height(3.dp))
        KeypadRow(listOf("4", "5", "6", "-"), onKey)
        Spacer(Modifier.height(3.dp))
        KeypadRow(listOf("1", "2", "3", "."), onKey)
        Spacer(Modifier.height(3.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            NumKey("0", Modifier.weight(3f), onKey)
            NumKey("C", Modifier.weight(1f), onKey)
        }
    }
}

@Composable
private fun KeypadRow(keys: List<String>, onKey: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        for (k in keys) {
            NumKey(k, Modifier.weight(1f), onKey)
        }
    }
}

@Composable
private fun NumKey(
    label: String,
    modifier: Modifier,
    onKey: (String) -> Unit
) {
    val (bg, fg) = when (label) {
        "C" -> md_error.copy(alpha = 0.22f) to md_error
        "⌫" -> calcOperatorBg to calcOperatorText
        else -> calcFunctionBg to calcFunctionText
    }
    Box(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .clickable { onKey(label) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ============================ 结果框 ============================

@Composable
private fun ResultBox(state: CalcState) {
    val (label, color) = when (state) {
        is CalcState.Waiting -> "等待输入所有变量" to md_onSurfaceVariant
        is CalcState.Success -> formatResult(state.value) to md_tertiary
        is CalcState.Failure -> state.message to md_error
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(calcFunctionBg)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = color,
            fontSize = if (state is CalcState.Success) 18.sp else 12.sp,
            fontWeight = if (state is CalcState.Success) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatResult(v: Double): String {
    if (v.isNaN()) return "NaN"
    if (v.isInfinite()) return if (v > 0) "∞" else "-∞"
    if (v == v.toLong().toDouble() && kotlin.math.abs(v) < 1e15) {
        return v.toLong().toString()
    }
    return String.format(Locale.US, "%.10g", v)
}