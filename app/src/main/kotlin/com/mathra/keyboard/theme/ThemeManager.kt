package com.mathra.keyboard.theme

import android.content.Context
import com.mathra.keyboard.util.PrefsManager

/**
 * ThemeManager: single source of truth for the active keyboard theme.
 * Reads/writes the theme preference to SharedPreferences.
 * Used by KeyboardView (IME) and ThemeGalleryFragment (dashboard).
 */
object ThemeManager {

    private const val KEY_THEME = "active_theme"

    fun getCurrentTheme(context: Context): KeyboardTheme {
        val id = PrefsManager.getString(context, KEY_THEME, KeyboardTheme.DarkNeon.id)
        return KeyboardTheme.fromId(id)
    }

    fun setTheme(context: Context, theme: KeyboardTheme) {
        PrefsManager.putString(context, KEY_THEME, theme.id)
    }

    fun getAllThemes(): List<KeyboardTheme> = KeyboardTheme.all
}
