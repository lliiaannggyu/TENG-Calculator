package com.tengwear.jisuanqi.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

/** 捐赠人员名字用的金色 */
private val ThanksGold = Color(0xFFFFD700)

@Composable
fun AboutScreen(onDismiss: () -> Unit) {
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
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(md_primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\uD83E\uDEE1",
                        fontSize = 26.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "TENG 计算器",
                    color = md_primary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = "TENG JiSuanQi",
                    color = md_onSurfaceVariant,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "版本 1.0.0",
                    color = md_onSurfaceVariant,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(14.dp))

                InfoCard {
                    InfoRow(label = "应用", value = "TENG 计算器")
                    InfoRow(label = "版本", value = "1.0.0")
                    InfoRow(label = "平台", value = "Wear OS")
                    InfoRow(label = "开发者", value = "TENG")
                }

                Spacer(Modifier.height(10.dp))

                InfoCard {
                    Text(
                        text = "主要功能",
                        color = md_onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                    FeatureItem("基础计算 · 科学计算")
                    FeatureItem("函数绘图 · 方程求解")
                    FeatureItem("方程组 · 不等式")
                    FeatureItem("单调性 · 求导 · 定积分")
                    FeatureItem("线性回归 · 科学计数法")
                    FeatureItem("数字与汉字 · 进制转换")
                    FeatureItem("单位换算 · 化学配平")
                    FeatureItem("元素周期表")
                }

                Spacer(Modifier.height(10.dp))

                // ===== 开发者发言 =====
                InfoCard {
                    Text(
                        text = "开发者发言",
                        color = md_primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )

                    Text(
                        text = "本应用由 TENG 耗时 1.5 天熬夜制作。",
                        color = calcFunctionText,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )

                    Text(
                        text = "本应用没有 BUG，都是特性。",
                        color = calcFunctionText,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )

                    Text(
                        text = "欢迎加入交流反馈吐槽——全是人机、全是潜水、没有活人感的 Q 群：1081401508。",
                        color = calcFunctionText,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )

                    Text(
                        text = "本人做的应用都会提前发内测到这个群，所以欢迎加入。",
                        color = calcFunctionText,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )

                    Text(
                        text = "本应用制作导致我的 DS 账号被封了 3 天。",
                        color = calcFunctionText,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )

                    Text(
                        text = "如果这个应用对你有帮助，可以为我捐赠激励吗？0.01 也是心意，我真的很需要钱，拜托了。",
                        color = md_tertiary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )

                    Text(
                        text = "应用里有两个非常隐蔽的彩蛋，欢迎摸索，找到了奖励 0 刀乐。",
                        color = md_tertiary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }

                // ================= 致谢名单 =================
                Spacer(Modifier.height(14.dp))

                Text(
                    text = "致谢名单",
                    color = md_primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                // ===== 捐赠人员（名字金色）=====
                ThanksCard(
                    title = "捐赠人员",
                    names = listOf(
                        "怀屿雪",
                        "某位三星 W6C 用户"
                    ),
                    nameColor = ThanksGold,
                    footer = "感谢你们的捐赠。每一笔都是真金白银的信任，也是我继续更新下去的动力。"
                )

                Spacer(Modifier.height(8.dp))

                // ===== 提供建议 =====
                ThanksCard(
                    title = "提供建议",
                    names = listOf(
                        "Joker.",
                        "怀屿雪",
                        "客厅的饭团子"
                    ),
                    nameColor = calcFunctionText,
                    footer = "感谢你们的宝贵建议。"
                )

                Spacer(Modifier.height(8.dp))

                // ===== 精神支持 =====
                ThanksCard(
                    title = "精神支持",
                    names = listOf(
                        "oride"
                    ),
                    nameColor = calcFunctionText,
                    footer = "感谢你们的精神陪伴。"
                )

                Spacer(Modifier.height(8.dp))

                // ===== 吉祥物 =====
                ThanksCard(
                    title = "吉祥物（宠物）",
                    names = listOf(
                        "teto耄耋",
                        "小茂镇压大肥鱼"
                    ),
                    nameColor = calcFunctionText,
                    footer = ""
                )

                Spacer(Modifier.height(8.dp))

                // ===== 遗漏说明 =====
                InfoCard {
                    Text(
                        text = "由于我懒得统计，如有遗漏请入群联系我：1081401508",
                        color = md_onSurfaceVariant,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "\u00A9 2026 TENG",
                    color = md_onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = calcEqualsBg,
                        contentColor = calcEqualsText
                    )
                ) {
                    Text(
                        text = "返回",
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
private fun InfoCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(calcNumberBg)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = md_onSurfaceVariant,
            fontSize = 11.sp,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = value,
            color = calcNumberText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(md_primary)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            color = calcFunctionText,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

/**
 * 致谢卡片
 *   - title：分区标题
 *   - names：名字列表
 *   - nameColor：名字颜色（捐赠组传金色，其他组传普通色）
 *   - footer：名字下方的感谢语，传空字符串则不显示
 */
@Composable
private fun ThanksCard(
    title: String,
    names: List<String>,
    nameColor: Color,
    footer: String
) {
    InfoCard {
        Text(
            text = title,
            color = md_primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        names.forEach { name ->
            Text(
                text = name,
                color = nameColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }

        if (footer.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = footer,
                color = calcFunctionText,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }
    }
}