package com.tengwear.jisuanqi.logic

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.tengwear.jisuanqi.data.SettingsRepository

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SettingsRepository(application)

    // ============ 显示缩放 ============

    var scaleFactor by mutableFloatStateOf(repo.scaleFactor)
        private set

    fun increaseScale() = updateScale(scaleFactor + SettingsRepository.SCALE_STEP)
    fun decreaseScale() {
        val next = scaleFactor - SettingsRepository.SCALE_STEP
        updateScale(next.coerceAtLeast(SettingsRepository.MIN_SCALE))
    }
    fun resetScale() = updateScale(SettingsRepository.DEFAULT_SCALE)

    private fun updateScale(value: Float) {
        scaleFactor = value
        repo.scaleFactor = value
    }

    // ============ 键盘震动 ============

    var hapticEnabled by mutableStateOf(repo.hapticEnabled)
        private set

    fun toggleHaptic() {
        hapticEnabled = !hapticEnabled
        repo.hapticEnabled = hapticEnabled
    }

    // ============ 右滑禁止返回 ============

    var blockSwipeExit by mutableStateOf(repo.blockSwipeExit)
        private set

    fun toggleBlockSwipeExit() {
        blockSwipeExit = !blockSwipeExit
        repo.blockSwipeExit = blockSwipeExit
    }

    // ============ 首次启动引导 ============

    fun isFirstLaunchShown(): Boolean = repo.firstLaunchShown

    fun markFirstLaunchShown() {
        repo.firstLaunchShown = true
    }

    // ============ 功能拓展排序 ============

    var extensionOrder by mutableStateOf(parseOrder(repo.extensionOrder))
        private set

    fun moveExtensionUp(index: Int) {
        if (index <= 0) return
        moveExtension(index, index - 1)
    }

    fun moveExtensionDown(index: Int) {
        if (index < 0) return
        if (index >= currentEffectiveOrder().size - 1) return
        moveExtension(index, index + 1)
    }

    private fun moveExtension(fromIndex: Int, toIndex: Int) {
        val effective = currentEffectiveOrder().toMutableList()
        if (fromIndex !in effective.indices) return
        if (toIndex !in effective.indices) return
        val item = effective.removeAt(fromIndex)
        effective.add(toIndex, item)
        extensionOrder = effective
        repo.extensionOrder = effective.joinToString(",")
    }

    fun resetExtensionOrder() {
        extensionOrder = emptyList()
        repo.extensionOrder = ""
    }

    fun currentEffectiveOrder(): List<String> {
        val custom = extensionOrder
        val result = mutableListOf<String>()
        for (id in custom) {
            if (id in DEFAULT_ORDER && id !in result) result.add(id)
        }
        for (id in DEFAULT_ORDER) {
            if (id !in result) result.add(id)
        }
        return result
    }

    private fun parseOrder(s: String): List<String> {
        if (s.isBlank()) return emptyList()
        return s.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    companion object {
        val DEFAULT_ORDER: List<String> = listOf(
            "history", "notes", "templates", "plot", "equation", "system",
            "inequality", "inequality_system",
            "monotonicity", "derivative", "integral",
            "regression", "scientific",
            "scientific_calc",
            "number_chinese", "number_base", "unit_converter",
            "chemistry", "periodic_table"
        )
    }
}