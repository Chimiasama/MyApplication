package com.example.swadebuilder

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import kotlin.math.roundToInt

class FeedbackController(context: Context) {
    private val vibrator = context.getSystemService(Vibrator::class.java)

    fun play(hapticStrength: Int, soundVolume: Int) {
        triggerHaptics(hapticStrength)
        triggerSound(soundVolume)
    }

    private fun triggerHaptics(hapticStrength: Int) {
        val clamped = hapticStrength.coerceIn(0, 100)
        if (clamped <= 0) return

        val amplitude = (clamped * 2.55f).roundToInt().coerceIn(1, 255)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(45L, amplitude)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(45L)
        }
    }

    private fun triggerSound(soundVolume: Int) {
        val clamped = soundVolume.coerceIn(0, 100)
        if (clamped <= 0) return

        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, clamped)
        toneGenerator.startTone(ToneGenerator.TONE_PROP_PROMPT, 120)
        toneGenerator.release()
    }

    fun dispose() {
        // No resources to release when using ToneGenerator per play.
    }
}
