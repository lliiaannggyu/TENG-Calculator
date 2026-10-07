package com.tengwear.jisuanqi.data

import android.content.Context
import androidx.core.content.edit

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences(
        "jisuanqi_settings",
        Context.MODE_PRIVATE
    )

    var scaleFactor: Float
        get() = prefs.getFloat(KEY_SCALE_FACTOR, DEFAULT_SCALE)
        set(value) = prefs.edit { putFloat(KEY_SCALE_FACTOR, value) }

    var hapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, DEFAULT_HAPTIC)
        set(value) = prefs.edit { putBoolean(KEY_HAPTIC, value) }

    var blockSwipeExit: Boolean
        get() = prefs.getBoolean(KEY_BLOCK_SWIPE, DEFAULT_BLOCK_SWIPE)
        set(value) = prefs.edit { putBoolean(KEY_BLOCK_SWIPE, value) }

    var extensionOrder: String
        get() = prefs.getString(KEY_EXT_ORDER, "") ?: ""
        set(value) = prefs.edit { putString(KEY_EXT_ORDER, value) }

    var firstLaunchShown: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, false)
        set(value) = prefs.edit { putBoolean(KEY_FIRST_LAUNCH, value) }

    companion object {
        private const val KEY_SCALE_FACTOR = "scale_factor"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_BLOCK_SWIPE = "block_swipe_exit"
        private const val KEY_EXT_ORDER = "extension_order"
        private const val KEY_FIRST_LAUNCH = "first_launch_shown"

        const val DEFAULT_SCALE = 1.3f
        const val MIN_SCALE = 0.05f
        const val SCALE_STEP = 0.1f

        const val DEFAULT_HAPTIC = true
        const val DEFAULT_BLOCK_SWIPE = false
    }
}