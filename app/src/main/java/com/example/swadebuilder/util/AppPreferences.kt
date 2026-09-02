package com.example.swadebuilder.util

import android.content.Context
import androidx.core.content.edit
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
    private const val KEY_PULAR_SELECAO_REGRAS = "pular_selecao_regras"
    private const val KEY_MODO_PERICIA = "modo_selecao_pericia"

    enum class ModoSelecaoPericia {
        CARROSSEL_POPOVER,
        STEPPER_CORES,
        CHIPS_DIRETOS
    }

    data class GlobalPrefs(
        val hapticStrength: Int,
        val soundVolume: Int,
        val tabStyle: TabStyle,
        val showBookIcon: Boolean,
        val showDescHome: Boolean,
        val showSystemMessages: Boolean,
        val appTheme: AppTheme,
        val pularSelecaoRegras: Boolean,
        val modoSelecaoPericia: ModoSelecaoPericia = ModoSelecaoPericia.CARROSSEL_POPOVER
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

        val pularSelecaoRegras = prefs.getBoolean(KEY_PULAR_SELECAO_REGRAS, false)
        val modoPericiaStr = prefs.getString(KEY_MODO_PERICIA, ModoSelecaoPericia.CARROSSEL_POPOVER.name) ?: ModoSelecaoPericia.CARROSSEL_POPOVER.name
        val modoSelecaoPericia = try {
            ModoSelecaoPericia.valueOf(modoPericiaStr)
        } catch (e: IllegalArgumentException) {
            ModoSelecaoPericia.CARROSSEL_POPOVER
        }

        return GlobalPrefs(
            hapticStrength = haptics,
            soundVolume = sound,
            tabStyle = tabStyle,
            showBookIcon = showBookIcon,
            showDescHome = showDescHome,
            showSystemMessages = showSystemMessages,
            appTheme = appTheme,
            pularSelecaoRegras = pularSelecaoRegras,
            modoSelecaoPericia = modoSelecaoPericia
        )
    }

    fun savePrefs(
        context: Context,
        hapticStrength: Int,
        soundVolume: Int,
        tabStyle: TabStyle,
        showBookIcon: Boolean,
        showDescHome: Boolean,
        showSystemMessages: Boolean,
        appTheme: AppTheme,
        pularSelecaoRegras: Boolean,
        modoSelecaoPericia: ModoSelecaoPericia = ModoSelecaoPericia.CARROSSEL_POPOVER
    ) {
        context
            .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit {
                putInt(KEY_HAPTIC, hapticStrength)
                putInt(KEY_SOUND, soundVolume)
                putString(KEY_TAB_STYLE, tabStyle.name)
                putBoolean(KEY_SHOW_BOOK_ICON, showBookIcon)
                putBoolean(KEY_SHOW_DESC_HOME, showDescHome)
                putBoolean(KEY_SYSTEM_MESSAGES, showSystemMessages)
                putString(KEY_APP_THEME, appTheme.name)
                putBoolean(KEY_PULAR_SELECAO_REGRAS, pularSelecaoRegras)
                putString(KEY_MODO_PERICIA, modoSelecaoPericia.name)
            }
    }

}
