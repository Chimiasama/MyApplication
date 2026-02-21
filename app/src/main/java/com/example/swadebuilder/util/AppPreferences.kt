package com.example.swadebuilder.util

import android.content.Context
import com.example.swadebuilder.TabStyle
import com.example.swadebuilder.ui.theme.AppTheme

object AppPreferences {
    private const val PREF_FILE = "swadebuilder_prefs"
    private const val KEY_HAPTIC = "haptic_strength"
    private const val KEY_SOUND = "sound_volume"
    private const val KEY_TAB_STYLE = "tab_style"
    private const val KEY_SHOW_BOOK_ICON = "show_book_icon"
    private const val KEY_SHOW_DESC_HOME = "show_desc_home_v2"
    private const val KEY_SYSTEM_MESSAGES = "show_system_messages"
    private const val KEY_APP_THEME = "app_theme"

    data class GlobalPrefs(
        val hapticStrength: Int,
        val soundVolume: Int,
        val tabStyle: TabStyle,
        val showBookIcon: Boolean,
        val showDescHome: Boolean,
        val showSystemMessages: Boolean,
        val appTheme: AppTheme
    )

    fun loadPrefs(context: Context, defaultHaptics: Int, defaultSound: Int): GlobalPrefs {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val haptics = prefs.getInt(KEY_HAPTIC, defaultHaptics)
        val sound = prefs.getInt(KEY_SOUND, defaultSound)

        val tabStyleStr = prefs.getString(KEY_TAB_STYLE, TabStyle.TEXTO.name) ?: TabStyle.TEXTO.name
        val tabStyle = try {
            TabStyle.valueOf(tabStyleStr)
        } catch (e: IllegalArgumentException) {
            TabStyle.TEXTO
        }

        val showBookIcon = prefs.getBoolean(KEY_SHOW_BOOK_ICON, true)
        val showDescHome = prefs.getBoolean(KEY_SHOW_DESC_HOME, true)
        val showSystemMessages = prefs.getBoolean(KEY_SYSTEM_MESSAGES, true)

        val themeStr = prefs.getString(KEY_APP_THEME, AppTheme.DEFAULT.name) ?: AppTheme.DEFAULT.name
        val appTheme = try {
            AppTheme.valueOf(themeStr)
        } catch (e: IllegalArgumentException) {
            AppTheme.DEFAULT
        }

        return GlobalPrefs(haptics, sound, tabStyle, showBookIcon, showDescHome, showSystemMessages, appTheme)
    }

    fun savePrefs(
        context: Context,
        hapticStrength: Int,
        soundVolume: Int,
        tabStyle: TabStyle,
        showBookIcon: Boolean,
        showDescHome: Boolean,
        showSystemMessages: Boolean,
        appTheme: AppTheme
    ) {
        context
            .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HAPTIC, hapticStrength)
            .putInt(KEY_SOUND, soundVolume)
            .putString(KEY_TAB_STYLE, tabStyle.name)
            .putBoolean(KEY_SHOW_BOOK_ICON, showBookIcon)
            .putBoolean(KEY_SHOW_DESC_HOME, showDescHome)
            .putBoolean(KEY_SYSTEM_MESSAGES, showSystemMessages)
            .putString(KEY_APP_THEME, appTheme.name)
            .apply()
    }

    // Legacy support to avoid breaking existing calls if any
    fun loadFeedbackPrefs(context: Context, defaultHaptics: Int, defaultSound: Int): FeedbackPrefs {
        val p = loadPrefs(context, defaultHaptics, defaultSound)
        return FeedbackPrefs(p.hapticStrength, p.soundVolume)
    }

    fun saveFeedbackPrefs(context: Context, hapticStrength: Int, soundVolume: Int) {
         // This partial save is tricky because we need the other values.
         // However, in the current architecture, state holds the source of truth.
         // We should use savePrefs passing all values from state.
         // For now, we update only what we have, but SharedPreferences.Editor is not persistent across calls.
         // This legacy method might overwrite others with defaults if we are not careful,
         // but since we are replacing the usage in MainActivity, it should be fine.
         // To be safe, we read first.
         val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
         prefs.edit()
            .putInt(KEY_HAPTIC, hapticStrength)
            .putInt(KEY_SOUND, soundVolume)
            .apply()
    }

    data class FeedbackPrefs(
        val hapticStrength: Int,
        val soundVolume: Int
    )
}
