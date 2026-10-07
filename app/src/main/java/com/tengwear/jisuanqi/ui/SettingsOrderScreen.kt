package com.tengwear.jisuanqi.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.tengwear.jisuanqi.ui.theme.*

/**
 * 功能拓展列表排序页面
 *
 * @param order    当前生效的完整 ID 顺序
 * @param onMoveUp 上移（参数为当前 index）
 * @param onMoveDown 下移
 * @param onReset  恢复默认顺序
 * @param onDismiss 返回
 */
@Composable
fun SettingsOrderScreen(
    order: List<String>,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val columnState = rememberTransformingLazyColumnState()
    val spec = rememberTransformationSpec()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            TransformingLazyColumn(
                state = columnState,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                item {
                    Text(
                        text = "功能拓展排序",
                        color = md_primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                            .padding(vertical = 4.dp)
                    )
                }

                item {
                    Text(
                        text = "用 ↑ ↓ 调整顺序，立即生效",
                        color = md_onSurfaceVariant,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec)
                    )
                }

                items(order.size) { idx ->
                    val id = order[idx]
                    OrderRow(
                        index = idx,
                        title = ExtensionItems.titleOf(id),
                        subtitle = ExtensionItems.subtitleOf(id),
                        canMoveUp = idx > 0,
                        canMoveDown = idx < order.size - 1,
                        onMoveUp = { onMoveUp(idx) },
                        onMoveDown = { onMoveDown(idx) },
                        modifier = Modifier.transformedHeight(this, spec)
                    )
                }

                item {
                    Spacer(Modifier.height(6.dp))
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, spec),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OrderActionButton(label = "默认", onClick = onReset)
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = calcEqualsBg,
                                contentColor = calcEqualsText
                            )
                        ) {
                            Text(
                                text = "完成",
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

@Composable
private fun OrderRow(
    index: Int,
    title: String,
    subtitle: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(calcNumberBg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${index + 1}.",
            color = md_onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.padding(end = 6.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = calcNumberText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = md_onSurfaceVariant,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        ArrowButton(
            label = "\u2191",
            enabled = canMoveUp,
            onClick = onMoveUp
        )
        Spacer(Modifier.size(4.dp))
        ArrowButton(
            label = "\u2193",
            enabled = canMoveDown,
            onClick = onMoveDown
        )
    }
}

@Composable
private fun ArrowButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg = if (enabled) calcFunctionBg else calcFunctionBg.copy(alpha = 0.4f)
    val fg = if (enabled) calcFunctionText else calcFunctionText.copy(alpha = 0.35f)

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun OrderActionButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(calcFunctionBg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = calcFunctionText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}