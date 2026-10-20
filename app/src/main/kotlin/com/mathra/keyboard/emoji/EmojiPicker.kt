package com.mathra.keyboard.emoji

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * EmojiPicker: a self-contained View that shows a scrollable emoji grid
 * with category tabs at the top, a search bar, and a recent/favorites tab.
 *
 * Usage: add as a child of keyboardContainer; callback fires on selection.
 */
class EmojiPicker(
    context: Context,
    private val onEmojiSelected: (String) -> Unit
) : LinearLayout(context) {

    private val categoryBar: HorizontalScrollView
    private val categoryBtnContainer: LinearLayout
    private val searchBar: EditText
    private val emojiGrid: RecyclerView
    private val emojiAdapter: EmojiGridAdapter

    private var currentCategory = -1  // -1 = recent, 0+ = category index
    private var searchQuery = ""

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.parseColor("#0D0D1A"))

        // ── Search bar ──────────────────────────────────────────────────
        searchBar = EditText(context).apply {
            hint = "Search emoji..."
            setHintTextColor(Color.parseColor("#888888"))
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1E1E3A"))
            setPadding(24, 12, 24, 12)
            textSize = 14f
            setSingleLine()
            addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    searchQuery = s?.toString() ?: ""
                    refreshGrid()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            })
        }
        addView(searchBar, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        // ── Category tab bar ────────────────────────────────────────────
        categoryBtnContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            setBackgroundColor(Color.parseColor("#12122A"))
        }
        categoryBar = HorizontalScrollView(context).apply {
            isHorizontalScrollBarEnabled = false
            addView(categoryBtnContainer)
        }
        addView(categoryBar, LayoutParams(LayoutParams.MATCH_PARENT,
            (48 * resources.displayMetrics.density).toInt()))

        // Recent tab
        addCategoryTab("🕐", -1)

        // Category tabs
        EmojiRepository.categories.forEachIndexed { index, cat ->
            addCategoryTab(cat.icon, index)
        }

        // ── Emoji grid ──────────────────────────────────────────────────
        emojiAdapter = EmojiGridAdapter { emoji ->
            RecentEmojis.addRecent(context, emoji)
            onEmojiSelected(emoji)
        }
        emojiGrid = RecyclerView(context).apply {
            layoutManager = GridLayoutManager(context, 8)
            adapter = emojiAdapter
            setBackgroundColor(Color.parseColor("#0D0D1A"))
        }
        addView(emojiGrid, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        // Default: show recent
        selectCategory(-1)
    }

    private fun addCategoryTab(icon: String, index: Int) {
        val btn = TextView(context).apply {
            text = icon
            textSize = 20f
            gravity = Gravity.CENTER
            setPadding(16, 8, 16, 8)
            setOnClickListener { selectCategory(index) }
        }
        categoryBtnContainer.addView(
            btn,
            LinearLayout.LayoutParams(
                (44 * resources.displayMetrics.density).toInt(),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
    }

    private fun selectCategory(index: Int) {
        currentCategory = index
        searchBar.text.clear()
        searchQuery = ""
        refreshGrid()
    }

    private fun refreshGrid() {
        val emojis = when {
            searchQuery.isNotEmpty() -> {
                // Search across all categories
                EmojiRepository.categories.flatMap { it.emojis }
                    .filter { it.contains(searchQuery) }
            }
            currentCategory == -1 -> {
                RecentEmojis.getRecent(context).ifEmpty {
                    EmojiRepository.categories.first().emojis.take(16)
                }
            }
            else -> EmojiRepository.categories[currentCategory].emojis
        }
        emojiAdapter.setEmojis(emojis)
    }
}

// ─── Emoji Grid RecyclerView Adapter ─────────────────────────────────────────

class EmojiGridAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<EmojiGridAdapter.VH>() {

    private var emojis = listOf<String>()

    fun setEmojis(list: List<String>) {
        emojis = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            textSize = 24f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                (44 * parent.context.resources.displayMetrics.density).toInt()
            )
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val emoji = emojis[position]
        (holder.itemView as TextView).text = emoji
        holder.itemView.setOnClickListener { onClick(emoji) }
    }

    override fun getItemCount() = emojis.size

    class VH(v: View) : RecyclerView.ViewHolder(v)
}
