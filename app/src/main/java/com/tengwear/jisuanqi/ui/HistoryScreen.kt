package com.tengwear.jisuanqi.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.tengwear.jisuanqi.logic.HistoryEntry
import com.tengwear.jisuanqi.logic.HistoryGroup
import com.tengwear.jisuanqi.ui.theme.*

@Composable
fun HistoryScreen(
    historyGroups: List<HistoryGroup>,
    onInsertResult: (String) -> Unit,
    onInsertExpression: (HistoryEntry) -> Unit,
    onInsertGroup: (HistoryGroup) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    var expandedGroupId by remember { mutableStateOf<Long?>(null) }
    var pendingEntry by remember { mutableStateOf<HistoryEntry?>(null) }

    val columnState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()

    // ★ 最外层 Box：让 Dialog 与 AppScaffold 同级
    Box(modifier = Modifier.fillMaxSize()) {

        AppScaffold {
            ScreenScaffold(
                scrollState = columnState,
                timeText = { TimeText() }
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    TransformingLazyColumn(
                        state = columnState,
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 8.dp,
                            start = 4.dp,
                            end = 4.dp
                        ),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        // ===== Header =====
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, spec)
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "历史记录",
                                    color = md_primary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(calcFunctionBg)
                                        .clickable { onClearHistory() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "清",
                                        color = calcFunctionText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // ===== 空态 =====
                        if (historyGroups.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .transformedHeight(this, spec)
                                        .padding(vertical = 30.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "暂无记录",
                                        color = md_onSurfaceVariant.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        // ===== 分组卡片 =====
                        items(historyGroups.size) { index ->
                            val group = historyGroups[index]
                            HistoryGroupCard(
                                group = group,
                                expanded = expandedGroupId == group.id,
                                onToggleExpand = {
                                    expandedGroupId =
                                        if (expandedGroupId == group.id) null else group.id
                                },
                                onEntryClick = { pendingEntry = it },
                                onInsertGroup = { onInsertGroup(group) },
                                modifier = Modifier.transformedHeight(this, spec)
                            )
                        }

                        // ===== 底部返回 =====
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .transformedHeight(this, spec)
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = calcEqualsBg,
                                        contentColor = calcEqualsText
                                    )
                                ) {
                                    Text(
                                        text = "返回拓展",
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

        // ★ 用 Dialog 铺满整个窗口，不受 Scaffold padding 影响
        pendingEntry?.let { entry ->
            Dialog(
                onDismissRequest = { pendingEntry = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false
                )
            ) {
                InsertMenu(
                    entry = entry,
                    onInsertExpression = {
                        onInsertExpression(entry)
                        pendingEntry = null
                    },
                    onInsertResult = {
                        onInsertResult(entry.result)
                        pendingEntry = null
                    },
                    onDismiss = { pendingEntry = null }
                )
            }
        }
    }
}

/**
 * 一组历史记录的卡片（手写布局）
 */
@Composable
private fun HistoryGroupCard(
    group: HistoryGroup,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onEntryClick: (HistoryEntry) -> Unit,
    onInsertGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleText: String = remember(group.id) {
        val last = group.entries.lastOrNull()
        if (last == null) "空记录"
        else "${prettyExpression(last.expression)} ${last.result}"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(md_surfaceVariant.copy(alpha = 0.35f))
            .clickable { onToggleExpand() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // 标题行
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (expanded) "\u25BC" else "\u25B6",
                color = md_primary,
                fontSize = 10.sp
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = titleText,
                color = md_onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = "${group.entries.size} 条",
                color = md_onSurfaceVariant,
                fontSize = 10.sp
            )
        }

        // 展开内容
        if (expanded) {
            Spacer(Modifier.height(4.dp))
            group.entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onEntryClick(entry) }
                        .padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prettyExpression(entry.expression),
                        color = md_onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = entry.result,
                        color = md_primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(calcFunctionBg)
                    .clickable { onInsertGroup() }
                    .padding(vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "整组插入",
                    color = calcFunctionText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 全屏弹出菜单
 *   - 纯色不透明背景，铺满整个窗口
 *   - 内容可上下滚动，避免按钮被挤出圆屏
 */
@Composable
private fun InsertMenu(
    entry: HistoryEntry,
    onInsertExpression: () -> Unit,
    onInsertResult: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(md_surface)          // 纯色不透明背景
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)  // 可上下滑动
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .clickable(enabled = false) {},
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // ===== 顶部：表达式 + 结果 =====
            Text(
                text = prettyExpression(entry.expression),
                color = md_onSurfaceVariant,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "= ${entry.result}",
                color = md_primary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            // ===== 插入算式 =====
            Button(
                onClick = onInsertExpression,
                colors = ButtonDefaults.buttonColors(
                    containerColor = calcOperatorBg,
                    contentColor = calcOperatorText
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "插入算式",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(6.dp))

            // ===== 插入结果 =====
            Button(
                onClick = onInsertResult,
                colors = ButtonDefaults.buttonColors(
                    containerColor = calcEqualsBg,
                    contentColor = calcEqualsText
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "插入结果",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(6.dp))

            // ===== 复制（结果 → 系统剪贴板）=====
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                            as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText("result", entry.result)
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = calcFunctionBg,
                    contentColor = calcFunctionText
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "复制",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(10.dp))

            // ===== 取消 =====
            Text(
                text = "取消",
                color = md_onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onDismiss() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}