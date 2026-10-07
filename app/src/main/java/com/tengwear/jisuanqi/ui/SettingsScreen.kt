package com.tengwear.jisuanqi.ui

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.tengwear.jisuanqi.ui.theme.*

@Composable
fun SettingsScreen(
    scaleFactor: Float,
    hapticEnabled: Boolean,
    blockSwipeExit: Boolean,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onReset: () -> Unit,
    onToggleHaptic: () -> Unit,
    onToggleBlockSwipeExit: () -> Unit,
    onOpenOrder: () -> Unit,
    onOpenAbout: () -> Unit,
    onDismiss: () -> Unit
) {
    AppScaffold {
        ScreenScaffold(
            timeText = { TimeText() }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "设置",
                    color = md_primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(10.dp))

                // ============ 显示大小 ============
                SectionCard {
                    Text(
                        text = "显示大小",
                        color = md_onSurfaceVariant,
                        fontSize = 11.sp
                    )

                    Text(
                        text = String.format("%.1fx", scaleFactor),
                        color = md_onSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RoundSettingButton(label = "-", onClick = onDecrease)
                        RoundSettingButton(label = "＋", onClick = onIncrease)
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "恢复默认",
                        color = md_onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onReset() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // ============ 键盘震动 ============
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "键盘震动",
                                color = md_onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (hapticEnabled) "按下按键时震动" else "已关闭",
                                color = md_onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                        ToggleChip(
                            checked = hapticEnabled,
                            onToggle = onToggleHaptic
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ============ 右滑禁止返回 ============
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "右滑禁止返回",
                                color = md_onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (blockSwipeExit) "开启后需点页面按钮返回"
                                else "可从左边缘右滑返回",
                                color = md_onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                        ToggleChip(
                            checked = blockSwipeExit,
                            onToggle = onToggleBlockSwipeExit
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ============ 功能拓展排序 ============
                SectionCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenOrder() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "功能拓展排序",
                                color = md_onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "调整拓展菜单的显示顺序",
                                color = md_onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "\u203A",
                            color = md_primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // ============ 关于 ============
                SectionCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAbout() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "关于",
                                color = md_onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "版本信息 · 开发者",
                                color = md_onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                        Text(
                            text = "\u203A",
                            color = md_primary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = calcEqualsBg,
                        contentColor = calcEqualsText
                    )
                ) {
                    Text(
                        text = "返回计算器",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(calcNumberBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun RoundSettingButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(calcFunctionBg)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = calcFunctionText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ToggleChip(checked: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 46.dp, height = 26.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (checked) md_primary else md_surfaceVariant)
            .clickable { onToggle() },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 3.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(if (checked) md_onPrimary else md_onSurfaceVariant)
        )
    }
}