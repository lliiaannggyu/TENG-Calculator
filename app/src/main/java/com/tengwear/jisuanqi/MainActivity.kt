package com.tengwear.jisuanqi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Text
import com.tengwear.jisuanqi.logic.CalculatorViewModel
import com.tengwear.jisuanqi.logic.SettingsViewModel
import com.tengwear.jisuanqi.ui.AboutScreen
import com.tengwear.jisuanqi.ui.CalculatorScreen
import com.tengwear.jisuanqi.ui.ChemistryBalanceScreen
import com.tengwear.jisuanqi.ui.DerivativeScreen
import com.tengwear.jisuanqi.ui.EggScreen
import com.tengwear.jisuanqi.ui.EquationSolverScreen
import com.tengwear.jisuanqi.ui.EquationSystemScreen
import com.tengwear.jisuanqi.ui.ExtensionScreen
import com.tengwear.jisuanqi.ui.FunctionPlotScreen
import com.tengwear.jisuanqi.ui.HistoryScreen
import com.tengwear.jisuanqi.ui.InequalityScreen
import com.tengwear.jisuanqi.ui.InequalitySystemScreen
import com.tengwear.jisuanqi.ui.IntegralScreen
import com.tengwear.jisuanqi.ui.MonotonicityScreen
import com.tengwear.jisuanqi.ui.NotesScreen
import com.tengwear.jisuanqi.ui.NumberBaseScreen
import com.tengwear.jisuanqi.ui.NumberChineseScreen
import com.tengwear.jisuanqi.ui.PeriodicTableScreen
import com.tengwear.jisuanqi.ui.RegressionScreen
import com.tengwear.jisuanqi.ui.ScientificCalcScreen
import com.tengwear.jisuanqi.ui.ScientificNotationScreen
import com.tengwear.jisuanqi.ui.SettingsOrderScreen
import com.tengwear.jisuanqi.ui.SettingsScreen
import com.tengwear.jisuanqi.ui.TemplatesScreen
import com.tengwear.jisuanqi.ui.UnitConverterScreen
import com.tengwear.jisuanqi.ui.ValenceScreen
import com.tengwear.jisuanqi.ui.theme.JiSuanQiTheme
import com.tengwear.jisuanqi.ui.theme.md_primary
import kotlinx.coroutines.delay

private const val SCREEN_CALCULATOR         = "calculator"
private const val SCREEN_SETTINGS           = "settings"
private const val SCREEN_SETTINGS_ORDER     = "settings_order"
private const val SCREEN_ABOUT              = "about"
private const val SCREEN_EXTENSION          = "extension"
private const val SCREEN_HISTORY            = "history"
private const val SCREEN_NOTES              = "notes"
private const val SCREEN_TEMPLATES          = "templates"
private const val SCREEN_PLOT               = "plot"
private const val SCREEN_EQUATION           = "equation"
private const val SCREEN_SYSTEM             = "system"
private const val SCREEN_INEQUALITY         = "inequality"
private const val SCREEN_INEQUALITY_SYSTEM  = "inequality_system"
private const val SCREEN_MONOTONICITY       = "monotonicity"
private const val SCREEN_DERIVATIVE         = "derivative"
private const val SCREEN_INTEGRAL           = "integral"
private const val SCREEN_REGRESSION         = "regression"
private const val SCREEN_SCIENTIFIC         = "scientific"
private const val SCREEN_SCIENTIFIC_CALC    = "scientific_calc"
private const val SCREEN_NUMBER_CHINESE     = "number_chinese"
private const val SCREEN_NUMBER_BASE        = "number_base"
private const val SCREEN_UNIT_CONVERTER     = "unit_converter"
private const val SCREEN_CHEMISTRY          = "chemistry"
private const val SCREEN_PERIODIC_TABLE     = "periodic_table"
private const val SCREEN_VALENCE            = "valence"
private const val SCREEN_EGG_UPPER          = "egg_upper"
private const val SCREEN_EGG_LOWER          = "egg_lower"

private fun screenDepth(screen: String): Int = when (screen) {
    SCREEN_CALCULATOR -> 0
    SCREEN_SETTINGS, SCREEN_EXTENSION -> 1
    else -> 2
}

private enum class NavDir { H_FORWARD, H_BACKWARD, V_FROM_TOP, V_FROM_BOTTOM }

private fun navDirection(from: String, to: String): NavDir {
    if (from == SCREEN_CALCULATOR && to == SCREEN_SETTINGS) return NavDir.V_FROM_TOP
    if (from == SCREEN_SETTINGS && to == SCREEN_CALCULATOR) return NavDir.V_FROM_BOTTOM
    if (from == SCREEN_CALCULATOR && to == SCREEN_EXTENSION) return NavDir.V_FROM_BOTTOM
    if (from == SCREEN_EXTENSION && to == SCREEN_CALCULATOR) return NavDir.V_FROM_TOP
    return if (screenDepth(to) >= screenDepth(from)) NavDir.H_FORWARD else NavDir.H_BACKWARD
}

private fun backTarget(screen: String): String = when (screen) {
    SCREEN_SETTINGS          -> SCREEN_CALCULATOR
    SCREEN_SETTINGS_ORDER    -> SCREEN_SETTINGS
    SCREEN_ABOUT             -> SCREEN_SETTINGS
    SCREEN_EXTENSION         -> SCREEN_CALCULATOR
    SCREEN_HISTORY           -> SCREEN_EXTENSION
    SCREEN_NOTES             -> SCREEN_EXTENSION
    SCREEN_TEMPLATES         -> SCREEN_EXTENSION
    SCREEN_PLOT              -> SCREEN_EXTENSION
    SCREEN_EQUATION          -> SCREEN_EXTENSION
    SCREEN_SYSTEM            -> SCREEN_EXTENSION
    SCREEN_INEQUALITY        -> SCREEN_EXTENSION
    SCREEN_INEQUALITY_SYSTEM -> SCREEN_EXTENSION
    SCREEN_MONOTONICITY      -> SCREEN_EXTENSION
    SCREEN_DERIVATIVE        -> SCREEN_EXTENSION
    SCREEN_INTEGRAL          -> SCREEN_EXTENSION
    SCREEN_REGRESSION        -> SCREEN_EXTENSION
    SCREEN_SCIENTIFIC        -> SCREEN_EXTENSION
    SCREEN_SCIENTIFIC_CALC   -> SCREEN_EXTENSION
    SCREEN_NUMBER_CHINESE    -> SCREEN_EXTENSION
    SCREEN_NUMBER_BASE       -> SCREEN_EXTENSION
    SCREEN_UNIT_CONVERTER    -> SCREEN_EXTENSION
    SCREEN_CHEMISTRY         -> SCREEN_EXTENSION
    SCREEN_PERIODIC_TABLE    -> SCREEN_EXTENSION
    SCREEN_VALENCE           -> SCREEN_EXTENSION
    SCREEN_EGG_UPPER         -> SCREEN_CHEMISTRY
    SCREEN_EGG_LOWER         -> SCREEN_CHEMISTRY
    else                     -> SCREEN_CALCULATOR
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JiSuanQiTheme {
                AppRoot(onFinish = { finish() })
            }
        }
    }
}

@Composable
private fun AppRoot(onFinish: () -> Unit) {
    val settingsViewModel: SettingsViewModel = viewModel()
    val calcViewModel: CalculatorViewModel = viewModel()

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        calcViewModel.onAppBackground()
    }

    var showIntro by remember {
        mutableStateOf(!settingsViewModel.isFirstLaunchShown())
    }
    if (showIntro) {
        FirstLaunchIntro(onDismiss = {
            settingsViewModel.markFirstLaunchShown()
            showIntro = false
        })
        return
    }

    var screen by rememberSaveable { mutableStateOf(SCREEN_CALCULATOR) }
    var plotExpression by rememberSaveable { mutableStateOf("sin(x)") }
    var plotXRange by rememberSaveable { mutableFloatStateOf(10f) }

    val extensionListState = rememberTransformingLazyColumnState()
    val pendingExit = remember { mutableStateOf(false) }

    val currentScreenState = rememberUpdatedState(screen)
    val blockSwipeExitState = rememberUpdatedState(settingsViewModel.blockSwipeExit)

    BackHandler(enabled = true) {
        if (blockSwipeExitState.value) return@BackHandler

        val current = currentScreenState.value
        if (current != SCREEN_CALCULATOR) {
            pendingExit.value = false
            screen = backTarget(current)
            return@BackHandler
        }

        if (!pendingExit.value) {
            pendingExit.value = true
        } else {
            calcViewModel.onAppBackground()
            onFinish()
        }
    }

    AnimatedContent(
        targetState = screen,
        contentKey = { it },
        transitionSpec = {
            val dir = navDirection(initialState, targetState)
            val slideSpec = tween<IntOffset>(durationMillis = 260)
            val fadeSpec = tween<Float>(durationMillis = 220)
            when (dir) {
                NavDir.V_FROM_TOP ->
                    (slideInVertically(slideSpec) { full -> -full } + fadeIn(fadeSpec)) togetherWith
                            (slideOutVertically(slideSpec) { full -> full } + fadeOut(fadeSpec))
                NavDir.V_FROM_BOTTOM ->
                    (slideInVertically(slideSpec) { full -> full } + fadeIn(fadeSpec)) togetherWith
                            (slideOutVertically(slideSpec) { full -> -full } + fadeOut(fadeSpec))
                NavDir.H_FORWARD ->
                    (slideInHorizontally(slideSpec) { full -> full / 2 } + fadeIn(fadeSpec)) togetherWith
                            (slideOutHorizontally(slideSpec) { full -> -full / 3 } + fadeOut(fadeSpec))
                NavDir.H_BACKWARD ->
                    (slideInHorizontally(slideSpec) { full -> -full / 3 } + fadeIn(fadeSpec)) togetherWith
                            (slideOutHorizontally(slideSpec) { full -> full / 2 } + fadeOut(fadeSpec))
            }
        },
        label = "screen_transition"
    ) { currentScreen ->
        when (currentScreen) {
            SCREEN_SETTINGS -> SettingsScreen(
                scaleFactor = settingsViewModel.scaleFactor,
                hapticEnabled = settingsViewModel.hapticEnabled,
                blockSwipeExit = settingsViewModel.blockSwipeExit,
                onIncrease = settingsViewModel::increaseScale,
                onDecrease = settingsViewModel::decreaseScale,
                onReset = settingsViewModel::resetScale,
                onToggleHaptic = settingsViewModel::toggleHaptic,
                onToggleBlockSwipeExit = settingsViewModel::toggleBlockSwipeExit,
                onOpenOrder = {
                    pendingExit.value = false
                    screen = SCREEN_SETTINGS_ORDER
                },
                onOpenAbout = {
                    pendingExit.value = false
                    screen = SCREEN_ABOUT
                },
                onDismiss = {
                    pendingExit.value = false
                    screen = SCREEN_CALCULATOR
                }
            )

            SCREEN_SETTINGS_ORDER -> SettingsOrderScreen(
                order = settingsViewModel.currentEffectiveOrder(),
                onMoveUp = settingsViewModel::moveExtensionUp,
                onMoveDown = settingsViewModel::moveExtensionDown,
                onReset = settingsViewModel::resetExtensionOrder,
                onDismiss = {
                    pendingExit.value = false
                    screen = SCREEN_SETTINGS
                }
            )

            SCREEN_ABOUT -> AboutScreen(
                onDismiss = {
                    pendingExit.value = false
                    screen = SCREEN_SETTINGS
                }
            )

            SCREEN_EXTENSION -> ExtensionScreen(
                order = settingsViewModel.currentEffectiveOrder(),
                columnState = extensionListState,
                onOpenHistory = {
                    pendingExit.value = false
                    calcViewModel.commitSession()
                    screen = SCREEN_HISTORY
                },
                onOpenNotes = { pendingExit.value = false; screen = SCREEN_NOTES },
                onOpenTemplates = { pendingExit.value = false; screen = SCREEN_TEMPLATES },
                onOpenPlot = { pendingExit.value = false; screen = SCREEN_PLOT },
                onOpenEquation = { pendingExit.value = false; screen = SCREEN_EQUATION },
                onOpenSystem = { pendingExit.value = false; screen = SCREEN_SYSTEM },
                onOpenInequality = { pendingExit.value = false; screen = SCREEN_INEQUALITY },
                onOpenInequalitySystem = { pendingExit.value = false; screen = SCREEN_INEQUALITY_SYSTEM },
                onOpenMonotonicity = { pendingExit.value = false; screen = SCREEN_MONOTONICITY },
                onOpenDerivative = { pendingExit.value = false; screen = SCREEN_DERIVATIVE },
                onOpenIntegral = { pendingExit.value = false; screen = SCREEN_INTEGRAL },
                onOpenRegression = { pendingExit.value = false; screen = SCREEN_REGRESSION },
                onOpenScientific = { pendingExit.value = false; screen = SCREEN_SCIENTIFIC },
                onOpenScientificCalc = { pendingExit.value = false; screen = SCREEN_SCIENTIFIC_CALC },
                onOpenNumberChinese = { pendingExit.value = false; screen = SCREEN_NUMBER_CHINESE },
                onOpenNumberBase = { pendingExit.value = false; screen = SCREEN_NUMBER_BASE },
                onOpenUnitConverter = { pendingExit.value = false; screen = SCREEN_UNIT_CONVERTER },
                onOpenChemistry = { pendingExit.value = false; screen = SCREEN_CHEMISTRY },
                onOpenPeriodicTable = { pendingExit.value = false; screen = SCREEN_PERIODIC_TABLE },
                onOpenValence = { pendingExit.value = false; screen = SCREEN_VALENCE },
                onDismiss = {
                    pendingExit.value = false
                    screen = SCREEN_CALCULATOR
                }
            )

            SCREEN_HISTORY -> HistoryScreen(
                historyGroups = calcViewModel.historyGroups,
                onInsertResult = { r ->
                    pendingExit.value = false
                    calcViewModel.insertResult(r)
                    screen = SCREEN_CALCULATOR
                },
                onInsertExpression = { e ->
                    pendingExit.value = false
                    calcViewModel.insertExpression(e)
                    screen = SCREEN_CALCULATOR
                },
                onInsertGroup = { g ->
                    pendingExit.value = false
                    calcViewModel.insertGroup(g)
                    screen = SCREEN_CALCULATOR
                },
                onClearHistory = calcViewModel::clearAllHistory,
                onDismiss = {
                    pendingExit.value = false
                    screen = SCREEN_EXTENSION
                }
            )

            SCREEN_NOTES -> NotesScreen(
                onDismiss = {
                    pendingExit.value = false
                    screen = SCREEN_EXTENSION
                }
            )

            SCREEN_TEMPLATES -> TemplatesScreen(
                onDismiss = {
                    pendingExit.value = false
                    screen = SCREEN_EXTENSION
                }
            )

            SCREEN_PLOT -> FunctionPlotScreen(
                expression = plotExpression,
                onExpressionChange = { plotExpression = it },
                xRange = plotXRange,
                onXRangeChange = { plotXRange = it },
                scaleFactor = settingsViewModel.scaleFactor,
                onDismiss = {
                    pendingExit.value = false
                    screen = SCREEN_EXTENSION
                }
            )

            SCREEN_EQUATION -> EquationSolverScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_SYSTEM -> EquationSystemScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_INEQUALITY -> InequalityScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_INEQUALITY_SYSTEM -> InequalitySystemScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_MONOTONICITY -> MonotonicityScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_DERIVATIVE -> DerivativeScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_INTEGRAL -> IntegralScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_REGRESSION -> RegressionScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_SCIENTIFIC -> ScientificNotationScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_SCIENTIFIC_CALC -> ScientificCalcScreen(
                scaleFactor = settingsViewModel.scaleFactor,
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_NUMBER_CHINESE -> NumberChineseScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_NUMBER_BASE -> NumberBaseScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_UNIT_CONVERTER -> UnitConverterScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_CHEMISTRY -> ChemistryBalanceScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION },
                onOpenEggUpper = {
                    pendingExit.value = false
                    screen = SCREEN_EGG_UPPER
                },
                onOpenEggLower = {
                    pendingExit.value = false
                    screen = SCREEN_EGG_LOWER
                }
            )

            SCREEN_PERIODIC_TABLE -> PeriodicTableScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_VALENCE -> ValenceScreen(
                onDismiss = { pendingExit.value = false; screen = SCREEN_EXTENSION }
            )

            SCREEN_EGG_UPPER -> EggScreen(
                variant = 0,
                onDismiss = {
                    pendingExit.value = false
                    screen = SCREEN_CHEMISTRY
                }
            )

            SCREEN_EGG_LOWER -> EggScreen(
                variant = 1,
                onDismiss = {
                    pendingExit.value = false
                    screen = SCREEN_CHEMISTRY
                }
            )

            else -> CalculatorScreen(
                viewModel = calcViewModel,
                scaleFactor = settingsViewModel.scaleFactor,
                hapticEnabled = settingsViewModel.hapticEnabled,
                onSwipeDown = {
                    pendingExit.value = false
                    screen = SCREEN_SETTINGS
                },
                onSwipeUp = {
                    pendingExit.value = false
                    screen = SCREEN_EXTENSION
                }
            )
        }
    }
}

@Composable
private fun FirstLaunchIntro(onDismiss: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(4000)
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "上下左右滑一滑，\n发现新大陆哟",
            color = md_primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}