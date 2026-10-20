package com.mathra.keyboard.util

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.view.KeyEvent

/**
 * SoundHelper: plays key-click sounds using the system AudioManager.
 * Uses AudioManager.playSoundEffect() — no audio files needed, no permissions.
 */
object SoundHelper {

    fun playKeyClick(context: Context) {
        if (!PrefsManager.getBoolean(context, PrefsManager.KEY_SOUND, true)) return

        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        audio.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, -1f)
    }

    fun playDelete(context: Context) {
        if (!PrefsManager.getBoolean(context, PrefsManager.KEY_SOUND, true)) return
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        audio.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, -1f)
    }

    fun playReturn(context: Context) {
        if (!PrefsManager.getBoolean(context, PrefsManager.KEY_SOUND, true)) return
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        audio.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN, -1f)
    }

    fun playSpacebar(context: Context) {
        if (!PrefsManager.getBoolean(context, PrefsManager.KEY_SOUND, true)) return
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        audio.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR, -1f)
    }
}
