package com.mathra.keyboard.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mathra.keyboard.R
import com.mathra.keyboard.theme.KeyboardTheme
import com.mathra.keyboard.theme.ThemeManager
import android.graphics.Color
import android.view.Gravity
import android.widget.FrameLayout

/**
 * ThemeGalleryFragment: displays all available themes as live preview cards.
 * User taps a card to apply the theme immediately.
 */
class ThemeGalleryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scrollView = android.widget.ScrollView(requireContext()).apply {
            setBackgroundColor(Color.parseColor("#0D0D1A"))
        }
        val container2 = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(requireContext()).apply {
            text = "🎨  Theme Gallery"
            textSize = 22f
            setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 24)
        }
        container2.addView(title)

        val active = ThemeManager.getCurrentTheme(requireContext())

        for (theme in KeyboardTheme.all) {
            val card = buildThemeCard(theme, theme.id == active.id)
            container2.addView(card)
        }

        scrollView.addView(container2)
        return scrollView
    }

    private fun buildThemeCard(theme: KeyboardTheme, isActive: Boolean): View {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(theme.keyboardBg)
            setPadding(24, 24, 24, 24)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 20)
            layoutParams = params
            elevation = 8f
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(theme.keyboardBg)
                setStroke(if (isActive) 3 else 1,
                    if (isActive) theme.accentColor else theme.keyBorder)
            }
        }

        // Theme name
        val name = TextView(requireContext()).apply {
            text = (if (isActive) "✓  " else "   ") + theme.displayName
            textSize = 18f
            setTextColor(if (isActive) theme.accentColor else Color.WHITE)
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        }
        card.addView(name)

        // Mini keyboard preview (3 sample keys)
        val keyRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 0)
        }
        listOf("Q", "W", "E", "R", "T", "Y").forEach { letter ->
            val key = buildSampleKey(letter, theme)
            keyRow.addView(key)
        }
        card.addView(keyRow)

        // Space + Enter preview
        val bottomRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 0)
        }
        bottomRow.addView(buildSampleKey("SPACE", theme, weight = 3f))
        bottomRow.addView(buildSampleKey("↵ ENTER", theme, isEnter = true))
        card.addView(bottomRow)

        card.setOnClickListener {
            ThemeManager.setTheme(requireContext(), theme)
            // Refresh the fragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, ThemeGalleryFragment())
                .commit()
        }

        return card
    }

    private fun buildSampleKey(
        label: String, theme: KeyboardTheme,
        weight: Float = 1f, isEnter: Boolean = false
    ): View {
        return TextView(requireContext()).apply {
            text = label
            textSize = 11f
            setTextColor(if (isEnter) Color.WHITE else theme.keyText)
            gravity = Gravity.CENTER
            setPadding(8, 12, 8, 12)
            background = if (isEnter) {
                android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    colors = intArrayOf(theme.enterStart, theme.enterEnd)
                    gradientType = android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
                    orientation = android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT
                }
            } else {
                android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    cornerRadius = 12f
                    setColor(theme.keyBg)
                    setStroke(1, theme.keyBorder)
                }
            }
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
            params.setMargins(4, 0, 4, 0)
            layoutParams = params
        }
    }
}
