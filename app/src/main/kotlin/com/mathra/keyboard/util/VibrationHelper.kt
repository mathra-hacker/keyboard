package com.mathra.keyboard.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * VibrationHelper: provides haptic feedback on key press.
 * Uses the modern VibrationEffect API on Android 8+ with proper fallback.
 */
object VibrationHelper {

    private const val VIBRATE_MS = 20L  // short 20ms pulse for key feedback

    fun vibrate(context: Context) {
        if (!PrefsManager.getBoolean(context, PrefsManager.KEY_VIBRATION, true)) return

        val vibrator = getVibrator(context) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(VIBRATE_MS, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VIBRATE_MS)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
