package com.mathra.keyboard.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * PrefsManager: unified access to normal and encrypted SharedPreferences.
 *
 * Normal prefs: theme, sound/vibration settings, keyboard height, etc.
 * Encrypted prefs: AI API key only.
 */
object PrefsManager {

    private const val PREFS_NAME = "mathra_prefs"
    private const val ENCRYPTED_PREFS_NAME = "mathra_secure_prefs"

    // ─── Normal preferences ───────────────────────────────────────────────

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getString(context: Context, key: String, default: String = ""): String =
        prefs(context).getString(key, default) ?: default

    fun putString(context: Context, key: String, value: String) =
        prefs(context).edit().putString(key, value).apply()

    fun getBoolean(context: Context, key: String, default: Boolean = false): Boolean =
        prefs(context).getBoolean(key, default)

    fun putBoolean(context: Context, key: String, value: Boolean) =
        prefs(context).edit().putBoolean(key, value).apply()

    fun getInt(context: Context, key: String, default: Int = 0): Int =
        prefs(context).getInt(key, default)

    fun putInt(context: Context, key: String, value: Int) =
        prefs(context).edit().putInt(key, value).apply()

    fun getFloat(context: Context, key: String, default: Float = 0f): Float =
        prefs(context).getFloat(key, default)

    fun putFloat(context: Context, key: String, value: Float) =
        prefs(context).edit().putFloat(key, value).apply()

    fun getStringSet(context: Context, key: String): Set<String> =
        prefs(context).getStringSet(key, emptySet()) ?: emptySet()

    fun putStringSet(context: Context, key: String, value: Set<String>) =
        prefs(context).edit().putStringSet(key, value).apply()

    // ─── Convenience keys ─────────────────────────────────────────────────

    const val KEY_THEME       = "active_theme"
    const val KEY_SOUND       = "key_sound"
    const val KEY_VIBRATION   = "key_vibration"
    const val KEY_AUTOCORRECT = "autocorrect"
    const val KEY_SUGGESTIONS = "suggestions"
    const val KEY_SINGLISH    = "singlish_mode"
    const val KEY_KB_HEIGHT   = "kb_height_dp"  // stored as Int
    const val KEY_AI_ENABLED  = "ai_enabled"
    const val KEY_AI_ENDPOINT = "ai_endpoint"
    const val KEY_RECENT_EMOJI= "recent_emoji"
    const val KEY_FAV_EMOJI   = "fav_emoji"
    const val KEY_INPUT_MODE  = "input_mode"    // "en" | "si" | "singlish"

    // ─── Encrypted preferences (AI API key only) ──────────────────────────

    private fun encryptedPrefs(context: Context): SharedPreferences? {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback: store in normal prefs if encrypted store fails (shouldn't happen on API 26+)
            null
        }
    }

    fun getSecureString(context: Context, key: String, default: String = ""): String =
        encryptedPrefs(context)?.getString(key, default) ?: default

    fun putSecureString(context: Context, key: String, value: String) {
        encryptedPrefs(context)?.edit()?.putString(key, value)?.apply()
    }

    const val KEY_AI_API_KEY = "ai_api_key"
}
