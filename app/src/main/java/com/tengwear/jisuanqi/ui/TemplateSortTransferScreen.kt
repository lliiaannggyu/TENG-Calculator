@file:Suppress("SpellCheckingInspection")

package com.tengwear.jisuanqi.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.tengwear.jisuanqi.logic.TemplateSortMode
import com.tengwear.jisuanqi.logic.TemplatesViewModel
import com.tengwear.jisuanqi.ui.theme.*

// ============================ 排序设置页 ============================

@Composable
fun TemplateSortScreen(
    viewModel: TemplatesViewModel,
    onBack: () -> Unit
) {
    val columnState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    val currentMode = viewModel.sortMode
    val isManual = currentMode == TemplateSortMode.MANUAL

    // 手动模式时，显示的是存储顺序；其他模式时把当前显示顺序展示出来
    val list = if (isManual) viewModel.templates else viewModel.displayed

    BackHandler(enabled = true) { onBack() }

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { padding ->
            TransformingLazyColumn(
                state = columnState,
                contentPadding = padding,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Text(
                        text = "排序设置",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (mode in TemplateSortMode.values()) {
                            val selected = mode == currentMode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (selected) md_primaryContainer else calcFunctionBg
                                    )
                                    .clickable { viewModel.changeSortMode(mode) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    color = if (selected) md_onPrimaryContainer else calcFunctionText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                if (isManual) {
                    item {
                        Text(
                            text = "手动模式：用 ⇧ / ⇩ 调整顺序",
                            color = md_onSurfaceVariant,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, spec)
                                .padding(vertical = 2.dp)
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "当前按「${currentMode.label}」排序，切换到手动可拖动",
                            color = md_onSurfaceVariant,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, spec)
                                .padding(vertical = 2.dp)
                        )
                    }
                }

                if (list.isEmpty()) {
                    item {
                        Text(
                            text = "还没有模板",
                            color = md_onSurfaceVariant,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .transformedHeight(this, spec)
                                .padding(vertical = 20.dp)
                        )
                    }
                } else {
                    items(
                        count = list.size,
                        key = { index -> list[index].id }
                    ) { index ->
                        val t = list[index]
                        SortRow(
                            name = t.name,
                            index = index,
                            total = list.size,
                            isManual = isManual,
                            modifier = Modifier.transformedHeight(this, spec),
                            onUp = { viewModel.moveUp(index) },
                            onDown = { viewModel.moveDown(index) }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(4.dp))
                }

                item {
                    MiniButton(
                        label = "返回",
                        bg = calcFunctionBg,
                        fg = calcFunctionText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                    ) { onBack() }
                }
            }
        }
    }
}

@Composable
private fun SortRow(
    name: String,
    index: Int,
    total: Int,
    isManual: Boolean,
    modifier: Modifier = Modifier,
    onUp: () -> Unit,
    onDown: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(calcFunctionBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            color = calcFunctionText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (isManual) {
            Spacer(Modifier.size(4.dp))
            ArrowButton("⇧", enabled = index > 0) { onUp() }
            Spacer(Modifier.size(4.dp))
            ArrowButton("⇩", enabled = index < total - 1) { onDown() }
        }
    }
}

@Composable
private fun ArrowButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val bg = if (enabled) calcOperatorBg else calcFunctionBg.copy(alpha = 0.5f)
    val fg = if (enabled) calcOperatorText else calcFunctionText.copy(alpha = 0.3f)
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

// ============================ 导入页 ============================

@Composable
fun TemplateImportScreen(
    viewModel: TemplatesViewModel,
    onImported: (Int) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var raw by remember { mutableStateOf("") }
    var parsed by remember { mutableStateOf<List<com.tengwear.jisuanqi.data.FormulaTemplate>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var imported by remember { mutableStateOf(false) }

    // 首次进入时读取剪贴板
    LaunchedEffect(Unit) {
        val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
        raw = text
        if (text.isBlank()) {
            error = "剪贴板为空"
        } else {
            val p = TemplatesViewModel.parseJson(text)
            if (p == null) {
                error = "剪贴板内容不是有效的模板数据"
            } else if (p.isEmpty()) {
                error = "剪贴板里没有模板"
            } else {
                parsed = p
            }
        }
    }

    fun reRead() {
        imported = false
        error = null
        parsed = null
        val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString().orEmpty()
        raw = text
        if (text.isBlank()) {
            error = "剪贴板为空"
            return
        }
        val p = TemplatesViewModel.parseJson(text)
        if (p == null) error = "剪贴板内容不是有效的模板数据"
        else if (p.isEmpty()) error = "剪贴板里没有模板"
        else parsed = p
    }

    BackHandler(enabled = true) { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "导入模板",
            color = md_primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "把 JSON 文本复制到剪贴板后，点“重新读取”",
            color = md_onSurfaceVariant,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        if (error != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(md_error.copy(alpha = 0.16f))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error!!,
                    color = md_error,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else if (parsed != null) {
            val list = parsed!!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(calcFunctionBg)
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "将导入 ${list.size} 条模板：",
                        color = md_secondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    for (t in list.take(8)) {
                        Text(
                            text = "· ${t.name}",
                            color = calcFunctionText,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (list.size > 8) {
                        Text(
                            text = "… 还有 ${list.size - 8} 条",
                            color = md_onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(calcFunctionBg)
                    .padding(10.dp)
            ) {
                Text(
                    text = if (raw.isBlank()) "（剪贴板为空）" else "解析中…",
                    color = md_onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MiniButton("重新读取", calcFunctionBg, calcFunctionText, Modifier.weight(1f)) {
                reRead()
            }
            val canImport = parsed != null && !imported
            MiniButton(
                label = if (imported) "已导入" else "确认导入",
                bg = if (canImport) calcEqualsBg else calcFunctionBg,
                fg = if (canImport) calcEqualsText else calcFunctionText.copy(alpha = 0.4f),
                modifier = Modifier.weight(1f)
            ) {
                if (!canImport) return@MiniButton
                val count = viewModel.importJson(raw)
                if (count >= 0) {
                    imported = true
                    onImported(count)
                } else {
                    error = "导入失败：数据格式错误"
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        MiniButton("返回", calcFunctionBg, calcFunctionText, Modifier.fillMaxWidth()) {
            onBack()
        }
    }
}

// ============================ 导出页 ============================

@Composable
fun TemplateExportScreen(
    ids: Set<Long>,
    viewModel: TemplatesViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val json = remember(ids) { viewModel.exportJson(ids) }
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(json) {
        if (json.isNotEmpty() && json != "[]") {
            clipboard.setPrimaryClip(ClipData.newPlainText("公式模板", json))
            copied = true
        }
    }

    BackHandler(enabled = true) { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "导出模板",
            color = md_primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = if (copied) "已复制 ${ids.size} 条模板到剪贴板"
            else "复制失败，请手动复制下方内容",
            color = if (copied) md_tertiary else md_error,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(calcFunctionBg)
                .padding(10.dp)
        ) {
            Text(
                text = json.ifEmpty { "（无数据）" },
                color = calcFunctionText,
                fontSize = 9.sp,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(8.dp))

        MiniButton("返回", calcEqualsBg, calcEqualsText, Modifier.fillMaxWidth()) {
            onBack()
        }
    }
}