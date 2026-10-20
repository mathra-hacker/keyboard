package com.mathra.keyboard.ime

import android.content.Context
import android.content.SharedPreferences

/**
 * ClipboardManager: stores up to 20 recent clipboard entries locally.
 * Entries are stored in SharedPreferences as a JSON-like delimited string.
 * No cloud sync, no personal data upload.
 */
class ClipboardManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mathra_clipboard", Context.MODE_PRIVATE)

    private val MAX_ITEMS = 20
    private val DELIMITER = "\u0000"  // null char as delimiter (won't appear in clipboard text)
    private val KEY_ITEMS = "clipboard_items"

    fun getAll(): List<String> {
        val raw = prefs.getString(KEY_ITEMS, "") ?: ""
        return if (raw.isEmpty()) emptyList()
        else raw.split(DELIMITER).filter { it.isNotEmpty() }
    }

    fun add(text: String) {
        if (text.isBlank()) return
        val items = getAll().toMutableList()
        items.remove(text)          // remove duplicate
        items.add(0, text)          // add to top
        val trimmed = items.take(MAX_ITEMS)
        prefs.edit().putString(KEY_ITEMS, trimmed.joinToString(DELIMITER)).apply()
    }

    fun remove(text: String) {
        val items = getAll().toMutableList()
        items.remove(text)
        prefs.edit().putString(KEY_ITEMS, items.joinToString(DELIMITER)).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_ITEMS).apply()
    }
}
