@file:Suppress("SpellCheckingInspection")

package com.tengwear.jisuanqi.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.tengwear.jisuanqi.data.FormulaTemplate
import com.tengwear.jisuanqi.logic.TemplatesViewModel
import com.tengwear.jisuanqi.ui.theme.*

private sealed interface TemplatesPage {
    object Home : TemplatesPage
    object Sort : TemplatesPage
    data class Edit(val id: Long?) : TemplatesPage
    data class Calc(val id: Long) : TemplatesPage
    object Import : TemplatesPage
    data class Export(val ids: Set<Long>) : TemplatesPage
}

@Composable
fun TemplatesScreen(
    onDismiss: () -> Unit,
    viewModel: TemplatesViewModel = viewModel()
) {
    var page by remember { mutableStateOf<TemplatesPage>(TemplatesPage.Home) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var multiSelect by remember { mutableStateOf(false) }

    fun resetSelection() {
        selectedIds = emptySet()
        multiSelect = false
    }

    when (val p = page) {
        is TemplatesPage.Home -> TemplatesHomePage(
            viewModel = viewModel,
            selectedIds = selectedIds,
            multiSelect = multiSelect,
            onToggleSelect = { id ->
                selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
            },
            onEnterMultiSelect = { id ->
                multiSelect = true
                selectedIds = setOf(id)
            },
            onSelectAll = { selectedIds = viewModel.templates.map { it.id }.toSet() },
            onCreate = { page = TemplatesPage.Edit(null) },
            onCalc = { id -> page = TemplatesPage.Calc(id) },
            onOpenSort = { page = TemplatesPage.Sort },
            onOpenImport = { page = TemplatesPage.Import },
            onExport = {
                if (selectedIds.isNotEmpty()) page = TemplatesPage.Export(selectedIds)
            },
            onDeleteSelected = {
                viewModel.delete(selectedIds)
                resetSelection()
            },
            onCancelMultiSelect = { resetSelection() },
            onDismiss = onDismiss
        )

        is TemplatesPage.Edit -> TemplateEditScreen(
            template = p.id?.let { viewModel.findById(it) },
            onSave = { name, formula ->
                val id = p.id
                if (id == null) viewModel.add(name, formula)
                else viewModel.update(id, name, formula)
                page = TemplatesPage.Home
            },
            onCancel = { page = TemplatesPage.Home }
        )

        is TemplatesPage.Calc -> {
            val t = viewModel.findById(p.id)
            if (t == null) {
                LaunchedEffect(p.id) { page = TemplatesPage.Home }
            } else {
                TemplateCalcScreen(
                    template = t,
                    onEdit = { page = TemplatesPage.Edit(t.id) },
                    onBack = { page = TemplatesPage.Home }
                )
            }
        }

        is TemplatesPage.Sort -> TemplateSortScreen(
            viewModel = viewModel,
            onBack = { page = TemplatesPage.Home }
        )

        is TemplatesPage.Import -> TemplateImportScreen(
            viewModel = viewModel,
            onImported = { _ -> page = TemplatesPage.Home },
            onBack = { page = TemplatesPage.Home }
        )

        is TemplatesPage.Export -> TemplateExportScreen(
            ids = p.ids,
            viewModel = viewModel,
            onBack = {
                resetSelection()
                page = TemplatesPage.Home
            }
        )
    }
}

// ============================ 主列表页 ============================

@Composable
private fun TemplatesHomePage(
    viewModel: TemplatesViewModel,
    selectedIds: Set<Long>,
    multiSelect: Boolean,
    onToggleSelect: (Long) -> Unit,
    onEnterMultiSelect: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onCreate: () -> Unit,
    onCalc: (Long) -> Unit,
    onOpenSort: () -> Unit,
    onOpenImport: () -> Unit,
    onExport: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCancelMultiSelect: () -> Unit,
    onDismiss: () -> Unit
) {
    val columnState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()
    val list = viewModel.displayed

    BackHandler(enabled = true) {
        if (multiSelect) onCancelMultiSelect() else onDismiss()
    }

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
                        text = if (multiSelect) "已选 ${selectedIds.size} 项 / 共 ${list.size} 项"
                        else "公式模板",
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

                if (list.isEmpty()) {
                    item {
                        Text(
                            text = "还没有模板\n点下方“新建”创建一条",
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
                        TemplateCard(
                            template = t,
                            selected = t.id in selectedIds,
                            multiSelect = multiSelect,
                            modifier = Modifier.transformedHeight(this, spec),
                            onClick = {
                                if (multiSelect) onToggleSelect(t.id) else onCalc(t.id)
                            },
                            onLongClick = {
                                if (multiSelect) onToggleSelect(t.id)
                                else onEnterMultiSelect(t.id)
                            }
                        )
                    }
                }

                // ===== 底部按钮区 =====
                if (multiSelect) {
                    item {
                        TwoButtonRow(
                            modifier = Modifier.transformedHeight(this, spec),
                            leftLabel = if (selectedIds.size == list.size) "取消全选" else "全选",
                            rightLabel = "导出",
                            leftBg = calcFunctionBg,
                            leftFg = calcFunctionText,
                            rightBg = calcEqualsBg,
                            rightFg = calcEqualsText,
                            onLeftClick = {
                                if (selectedIds.size == list.size) onCancelMultiSelect()
                                else onSelectAll()
                            },
                            onRightClick = onExport
                        )
                    }
                    item {
                        TwoButtonRow(
                            modifier = Modifier.transformedHeight(this, spec),
                            leftLabel = "删除",
                            rightLabel = "取消",
                            leftBg = md_error.copy(alpha = 0.22f),
                            leftFg = md_error,
                            rightBg = calcFunctionBg,
                            rightFg = calcFunctionText,
                            onLeftClick = onDeleteSelected,
                            onRightClick = onCancelMultiSelect
                        )
                    }
                } else {
                    item {
                        TwoButtonRow(
                            modifier = Modifier.transformedHeight(this, spec),
                            leftLabel = "＋ 新建",
                            rightLabel = "排序",
                            leftBg = calcEqualsBg,
                            leftFg = calcEqualsText,
                            rightBg = calcFunctionBg,
                            rightFg = calcFunctionText,
                            onLeftClick = onCreate,
                            onRightClick = onOpenSort
                        )
                    }
                    item {
                        TwoButtonRow(
                            modifier = Modifier.transformedHeight(this, spec),
                            leftLabel = "导入",
                            rightLabel = "返回",
                            leftBg = calcFunctionBg,
                            leftFg = calcFunctionText,
                            rightBg = calcFunctionBg,
                            rightFg = calcFunctionText,
                            onLeftClick = onOpenImport,
                            onRightClick = onDismiss
                        )
                    }
                }

                item { Spacer(Modifier.height(4.dp)) }
            }
        }
    }
}

// ============================ 卡片与按钮组件 ============================

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TemplateCard(
    template: FormulaTemplate,
    selected: Boolean,
    multiSelect: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bg = if (selected) md_primaryContainer else calcFunctionBg
    val fg = if (selected) md_onPrimaryContainer else calcFunctionText

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (multiSelect) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (selected) md_primary else Color.Transparent)
                    .border(1.dp, md_primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Text(
                        text = "✓",
                        color = md_onPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = template.name,
                color = fg,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = template.formula,
                color = fg.copy(alpha = 0.65f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun TwoButtonRow(
    leftLabel: String,
    rightLabel: String,
    leftBg: Color,
    leftFg: Color,
    rightBg: Color,
    rightFg: Color,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        MiniButton(leftLabel, leftBg, leftFg, Modifier.weight(1f)) { onLeftClick() }
        MiniButton(rightLabel, rightBg, rightFg, Modifier.weight(1f)) { onRightClick() }
    }
}

@Composable
internal fun MiniButton(
    label: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}