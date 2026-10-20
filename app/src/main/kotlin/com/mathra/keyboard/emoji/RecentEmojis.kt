package com.mathra.keyboard.emoji

import android.content.Context
import com.mathra.keyboard.util.PrefsManager

/**
 * Manages recently used and favourite emojis, stored in SharedPreferences.
 */
object RecentEmojis {

    private const val MAX_RECENT = 30

    fun getRecent(context: Context): List<String> {
        return PrefsManager.getStringSet(context, PrefsManager.KEY_RECENT_EMOJI)
            .toList()
            .take(MAX_RECENT)
    }

    fun addRecent(context: Context, emoji: String) {
        val set = PrefsManager.getStringSet(context, PrefsManager.KEY_RECENT_EMOJI).toMutableList()
        set.remove(emoji)
        set.add(0, emoji)
        PrefsManager.putStringSet(context, PrefsManager.KEY_RECENT_EMOJI, set.take(MAX_RECENT).toSet())
    }

    fun getFavorites(context: Context): List<String> {
        return PrefsManager.getStringSet(context, PrefsManager.KEY_FAV_EMOJI).toList()
    }

    fun toggleFavorite(context: Context, emoji: String) {
        val favs = PrefsManager.getStringSet(context, PrefsManager.KEY_FAV_EMOJI).toMutableSet()
        if (favs.contains(emoji)) favs.remove(emoji) else favs.add(emoji)
        PrefsManager.putStringSet(context, PrefsManager.KEY_FAV_EMOJI, favs)
    }
}
