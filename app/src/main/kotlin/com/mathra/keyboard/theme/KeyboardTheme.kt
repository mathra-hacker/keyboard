package com.mathra.keyboard.theme

import android.graphics.Color

/**
 * Sealed class representing all available keyboard themes.
 * Each theme carries all colors needed by KeyboardView to render itself.
 * Add new themes here — no other code changes needed.
 */
sealed class KeyboardTheme(
    val id: String,
    val displayName: String,

    // Keyboard background
    val keyboardBg: Int,

    // Regular key
    val keyBg: Int,
    val keyPressed: Int,
    val keyBorder: Int,
    val keyText: Int,
    val keySubText: Int,

    // Special keys (shift, delete, lang)
    val specialKeyBg: Int,
    val specialKeyText: Int,

    // Space key
    val spaceBg: Int,

    // Enter key (gradient — start/end)
    val enterStart: Int,
    val enterEnd: Int,

    // Suggestion bar
    val suggestionBg: Int,
    val suggestionText: Int,
    val suggestionDivider: Int,

    // Glow / accent
    val accentColor: Int,
    val glowColor: Int,
    val glowRadius: Float = 12f,

    // Emoji / number row
    val numRowBg: Int,
) {

    object DarkNeon : KeyboardTheme(
        id = "dark_neon",
        displayName = "Dark Neon",
        keyboardBg    = Color.parseColor("#12122A"),
        keyBg         = Color.parseColor("#1E1E3A"),
        keyPressed    = Color.parseColor("#2E2E58"),
        keyBorder     = Color.parseColor("#3D2B6B"),
        keyText       = Color.WHITE,
        keySubText    = Color.parseColor("#88CCFF"),
        specialKeyBg  = Color.parseColor("#160E30"),
        specialKeyText= Color.parseColor("#B0B0D0"),
        spaceBg       = Color.parseColor("#1A1A40"),
        enterStart    = Color.parseColor("#7B2FBE"),
        enterEnd      = Color.parseColor("#00E5FF"),
        suggestionBg  = Color.parseColor("#0D0D1A"),
        suggestionText= Color.parseColor("#00E5FF"),
        suggestionDivider = Color.parseColor("#2E2E60"),
        accentColor   = Color.parseColor("#00E5FF"),
        glowColor     = Color.parseColor("#7B2FBE"),
        glowRadius    = 14f,
        numRowBg      = Color.parseColor("#0D0D1A"),
    )

    object PurpleGalaxy : KeyboardTheme(
        id = "purple_galaxy",
        displayName = "Purple Galaxy",
        keyboardBg    = Color.parseColor("#120025"),
        keyBg         = Color.parseColor("#1F0040"),
        keyPressed    = Color.parseColor("#3A0075"),
        keyBorder     = Color.parseColor("#5A1A8A"),
        keyText       = Color.WHITE,
        keySubText    = Color.parseColor("#D7A8FF"),
        specialKeyBg  = Color.parseColor("#0D0020"),
        specialKeyText= Color.parseColor("#BF8FFF"),
        spaceBg       = Color.parseColor("#180035"),
        enterStart    = Color.parseColor("#9933FF"),
        enterEnd      = Color.parseColor("#FF66CC"),
        suggestionBg  = Color.parseColor("#0A0015"),
        suggestionText= Color.parseColor("#BF5FFF"),
        suggestionDivider = Color.parseColor("#3A0075"),
        accentColor   = Color.parseColor("#BF5FFF"),
        glowColor     = Color.parseColor("#9933FF"),
        glowRadius    = 16f,
        numRowBg      = Color.parseColor("#0A0015"),
    )

    object CyanGlow : KeyboardTheme(
        id = "cyan_glow",
        displayName = "Cyan Glow",
        keyboardBg    = Color.parseColor("#001E2E"),
        keyBg         = Color.parseColor("#00253A"),
        keyPressed    = Color.parseColor("#004060"),
        keyBorder     = Color.parseColor("#006080"),
        keyText       = Color.WHITE,
        keySubText    = Color.parseColor("#80FFEE"),
        specialKeyBg  = Color.parseColor("#001520"),
        specialKeyText= Color.parseColor("#00FFEA"),
        spaceBg       = Color.parseColor("#002030"),
        enterStart    = Color.parseColor("#00BCD4"),
        enterEnd      = Color.parseColor("#00FFEA"),
        suggestionBg  = Color.parseColor("#001520"),
        suggestionText= Color.parseColor("#00FFEA"),
        suggestionDivider = Color.parseColor("#004060"),
        accentColor   = Color.parseColor("#00FFEA"),
        glowColor     = Color.parseColor("#00BCD4"),
        glowRadius    = 12f,
        numRowBg      = Color.parseColor("#001520"),
    )

    object MidnightAMOLED : KeyboardTheme(
        id = "amoled",
        displayName = "Midnight AMOLED",
        keyboardBg    = Color.parseColor("#050505"),
        keyBg         = Color.parseColor("#111111"),
        keyPressed    = Color.parseColor("#222222"),
        keyBorder     = Color.parseColor("#333333"),
        keyText       = Color.WHITE,
        keySubText    = Color.parseColor("#AAAAAA"),
        specialKeyBg  = Color.parseColor("#000000"),
        specialKeyText= Color.parseColor("#BBBBBB"),
        spaceBg       = Color.parseColor("#0A0A0A"),
        enterStart    = Color.parseColor("#7B2FBE"),
        enterEnd      = Color.parseColor("#BB86FC"),
        suggestionBg  = Color.parseColor("#000000"),
        suggestionText= Color.parseColor("#BB86FC"),
        suggestionDivider = Color.parseColor("#222222"),
        accentColor   = Color.parseColor("#BB86FC"),
        glowColor     = Color.parseColor("#7B2FBE"),
        glowRadius    = 10f,
        numRowBg      = Color.parseColor("#000000"),
    )

    object Light : KeyboardTheme(
        id = "light",
        displayName = "Light",
        keyboardBg    = Color.parseColor("#E8E8E8"),
        keyBg         = Color.WHITE,
        keyPressed    = Color.parseColor("#D0D0D0"),
        keyBorder     = Color.parseColor("#C0C0C0"),
        keyText       = Color.parseColor("#1A1A1A"),
        keySubText    = Color.parseColor("#666666"),
        specialKeyBg  = Color.parseColor("#D0D0D0"),
        specialKeyText= Color.parseColor("#333333"),
        spaceBg       = Color.WHITE,
        enterStart    = Color.parseColor("#6C3483"),
        enterEnd      = Color.parseColor("#9B59B6"),
        suggestionBg  = Color.parseColor("#F0F0F0"),
        suggestionText= Color.parseColor("#6C3483"),
        suggestionDivider = Color.parseColor("#D0D0D0"),
        accentColor   = Color.parseColor("#6C3483"),
        glowColor     = Color.parseColor("#9B59B6"),
        glowRadius    = 6f,
        numRowBg      = Color.parseColor("#DDDDDD"),
    )

    companion object {
        val all: List<KeyboardTheme> = listOf(DarkNeon, PurpleGalaxy, CyanGlow, MidnightAMOLED, Light)

        fun fromId(id: String): KeyboardTheme =
            all.firstOrNull { it.id == id } ?: DarkNeon
    }
}
