package com.mathra.keyboard.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.mathra.keyboard.emoji.EmojiRepository

/**
 * EmojiSettingsFragment: shows category list overview (informational).
 */
class EmojiSettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scroll = ScrollView(requireContext()).apply { setBackgroundColor(Color.parseColor("#0D0D1A")) }
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32)
        }
        layout.addView(TextView(requireContext()).apply {
            text = "😊  Emoji Settings"
            textSize = 22f; setTextColor(Color.parseColor("#00E5FF"))
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.bottomMargin = 24; layoutParams = p
        })
        layout.addView(TextView(requireContext()).apply {
            text = "${EmojiRepository.categories.sumOf { it.emojis.size }} emoji across ${EmojiRepository.categories.size} categories"
            textSize = 14f; setTextColor(Color.parseColor("#B0B0D0"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.bottomMargin = 24; layoutParams = p
        })
        EmojiRepository.categories.forEach { cat ->
            layout.addView(TextView(requireContext()).apply {
                text = "${cat.icon}  ${cat.name}  (${cat.emojis.size})"
                textSize = 15f; setTextColor(Color.WHITE)
                setPadding(0, 12, 0, 12)
            })
        }
        scroll.addView(layout)
        return scroll
    }
}
