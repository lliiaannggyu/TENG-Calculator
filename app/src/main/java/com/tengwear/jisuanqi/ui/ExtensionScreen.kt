package com.tengwear.jisuanqi.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.tengwear.jisuanqi.ui.theme.*

private data class MenuEntry(val id: String, val onClick: () -> Unit)

@Composable
fun ExtensionScreen(
    order: List<String>,
    columnState: TransformingLazyColumnState,
    onOpenHistory: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenPlot: () -> Unit,
    onOpenEquation: () -> Unit,
    onOpenSystem: () -> Unit,
    onOpenInequality: () -> Unit,
    onOpenInequalitySystem: () -> Unit,
    onOpenMonotonicity: () -> Unit,
    onOpenDerivative: () -> Unit,
    onOpenIntegral: () -> Unit,
    onOpenRegression: () -> Unit,
    onOpenScientific: () -> Unit,
    onOpenScientificCalc: () -> Unit,
    onOpenNumberChinese: () -> Unit,
    onOpenNumberBase: () -> Unit,
    onOpenUnitConverter: () -> Unit,
    onOpenChemistry: () -> Unit,
    onOpenPeriodicTable: () -> Unit,
    onOpenValence: () -> Unit,
    onDismiss: () -> Unit
) {
    val resolvedOrder = remember(order) { ExtensionItems.resolveOrder(order) }

    val menuItems = remember(resolvedOrder) {
        resolvedOrder.map { id ->
            MenuEntry(
                id = id,
                onClick = when (id) {
                    ExtensionItems.HISTORY -> onOpenHistory
                    ExtensionItems.NOTES -> onOpenNotes
                    ExtensionItems.TEMPLATES -> onOpenTemplates
                    ExtensionItems.PLOT -> onOpenPlot
                    ExtensionItems.EQUATION -> onOpenEquation
                    ExtensionItems.SYSTEM -> onOpenSystem
                    ExtensionItems.INEQUALITY -> onOpenInequality
                    ExtensionItems.INEQUALITY_SYSTEM -> onOpenInequalitySystem
                    ExtensionItems.MONOTONICITY -> onOpenMonotonicity
                    ExtensionItems.DERIVATIVE -> onOpenDerivative
                    ExtensionItems.INTEGRAL -> onOpenIntegral
                    ExtensionItems.REGRESSION -> onOpenRegression
                    ExtensionItems.SCIENTIFIC -> onOpenScientific
                    ExtensionItems.SCIENTIFIC_CALC -> onOpenScientificCalc
                    ExtensionItems.NUMBER_CHINESE -> onOpenNumberChinese
                    ExtensionItems.NUMBER_BASE -> onOpenNumberBase
                    ExtensionItems.UNIT_CONVERTER -> onOpenUnitConverter
                    ExtensionItems.CHEMISTRY -> onOpenChemistry
                    ExtensionItems.PERIODIC_TABLE -> onOpenPeriodicTable
                    ExtensionItems.VALENCE -> onOpenValence
                    else -> ({})
                }
            )
        }
    }

    val transformationSpec = rememberTransformationSpec()

    AppScaffold {
        ScreenScaffold(
            scrollState = columnState,
            timeText = { TimeText() }
        ) { contentPadding ->
            TransformingLazyColumn(
                state = columnState,
                contentPadding = contentPadding,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    ListHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec)
                            .padding(vertical = 4.dp),
                        transformation = SurfaceTransformation(transformationSpec)
                    ) {
                        Text(
                            text = "功能拓展",
                            color = md_primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                items(
                    count = menuItems.size,
                    key = { index -> menuItems[index].id }
                ) { index ->
                    val item = menuItems[index]
                    Button(
                        onClick = item.onClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = calcFunctionBg,
                            contentColor = calcFunctionText
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = ExtensionItems.titleOf(item.id),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = ExtensionItems.subtitleOf(item.id),
                                fontSize = 10.sp,
                                color = calcFunctionText.copy(alpha = 0.65f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this, transformationSpec),
                        transformation = SurfaceTransformation(transformationSpec),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = calcEqualsBg,
                            contentColor = calcEqualsText
                        )
                    ) {
                        Text(
                            text = "返回计算器",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}