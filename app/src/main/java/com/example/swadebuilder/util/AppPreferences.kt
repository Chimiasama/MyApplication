package com.example.swadebuilder.util

import android.content.Context

object AppPreferences {
    private const val PREF_FILE = "swadebuilder_prefs"
    private const val KEY_HAPTIC = "haptic_strength"
    private const val KEY_SOUND = "sound_volume"

    data class FeedbackPrefs(
        val hapticStrength: Int,
        val soundVolume: Int
    )

    fun loadFeedbackPrefs(context: Context, defaultHaptics: Int, defaultSound: Int): FeedbackPrefs {
        val prefs = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
        val haptics = prefs.getInt(KEY_HAPTIC, defaultHaptics)
        val sound = prefs.getInt(KEY_SOUND, defaultSound)
        return FeedbackPrefs(haptics, sound)
    }

    fun saveFeedbackPrefs(context: Context, hapticStrength: Int, soundVolume: Int) {
        context
            .getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HAPTIC, hapticStrength)
            .putInt(KEY_SOUND, soundVolume)
            .apply()
    }
}
