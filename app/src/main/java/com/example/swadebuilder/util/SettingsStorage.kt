package com.example.swadebuilder.util

import android.content.Context
import android.content.SharedPreferences

object SettingsStorage {
    private const val PREF_FILE = "swadebuilder_settings"

    // Keys for settings
    const val KEY_WILDCARD = "opt_wildcard"
    const val KEY_MORE_SKILL_POINTS = "opt_more_skill_points"
    const val KEY_MULTI_ARCANE = "opt_multi_arcane"
    const val KEY_SPECIALIZATION = "opt_specialization"
    const val KEY_UNARMORED_HERO = "opt_unarmored_hero"
    const val KEY_MULTI_LANG = "opt_multi_lang"
    const val KEY_BORN_A_HERO = "opt_born_a_hero"
    const val KEY_NO_POWER_POINTS = "opt_no_power_points"

    const val KEY_SUPERS = "opt_supers"
    const val KEY_BIG_RESPONSIBILITIES = "opt_big_responsibilities"

    const val KEY_HORROR = "opt_horror"
    const val KEY_MONSTER_MODE = "opt_monster_mode"

    const val KEY_FANTASY = "opt_fantasy"
    const val KEY_BUSCATRILHA = "opt_buscatrilha"
    const val KEY_DEADLANDS = "opt_deadlands"
    const val KEY_CRYSTAL_HEART = "opt_crystal_heart"
    const val KEY_WAR_ARTS = "opt_war_arts"
    const val KEY_STEAM_SUN = "opt_steam_sun"
    const val KEY_WISEGUYS = "opt_wiseguys"

    const val KEY_SCIFI = "opt_scifi"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
    }

    fun saveBoolean(context: Context, key: String, value: Boolean) {
        getPrefs(context).edit().putBoolean(key, value).apply()
    }

    fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
        return getPrefs(context).getBoolean(key, defaultValue)
    }
}
