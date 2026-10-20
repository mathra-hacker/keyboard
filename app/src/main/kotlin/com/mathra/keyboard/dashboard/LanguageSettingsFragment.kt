package com.mathra.keyboard.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.mathra.keyboard.util.PrefsManager

/**
 * LanguageSettingsFragment: keyboard height, sound, vibration, autocorrect, suggestions, singlish toggle.
 */
class LanguageSettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scroll = ScrollView(requireContext()).apply {
            setBackgroundColor(Color.parseColor("#0D0D1A"))
        }
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        layout.addView(sectionTitle("⌨️  Keyboard Settings"))
        layout.addView(switchRow("Word Suggestions", PrefsManager.KEY_SUGGESTIONS, true))
        layout.addView(switchRow("Autocorrect", PrefsManager.KEY_AUTOCORRECT, true))
        layout.addView(switchRow("Singlish Mode", PrefsManager.KEY_SINGLISH, false))
        layout.addView(switchRow("Key Sound", PrefsManager.KEY_SOUND, true))
        layout.addView(switchRow("Vibration Feedback", PrefsManager.KEY_VIBRATION, true))
        layout.addView(sectionTitle("📏  Keyboard Height"))
        layout.addView(heightSlider())

        scroll.addView(layout)
        return scroll
    }

    private fun sectionTitle(text: String) = TextView(requireContext()).apply {
        this.text = text
        textSize = 18f
        setTextColor(Color.parseColor("#00E5FF"))
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.setMargins(0, 24, 0, 12)
        layoutParams = p
    }

    private fun switchRow(label: String, key: String, default: Boolean): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
        }
        val tv = TextView(requireContext()).apply {
            text = label
            textSize = 15f
            setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val sw = Switch(requireContext()).apply {
            isChecked = PrefsManager.getBoolean(requireContext(), key, default)
            setOnCheckedChangeListener { _, checked ->
                PrefsManager.putBoolean(requireContext(), key, checked)
            }
        }
        row.addView(tv)
        row.addView(sw)
        return row
    }

    private fun heightSlider(): View {
        val col = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        val label = TextView(requireContext()).apply {
            text = "Height: ${PrefsManager.getInt(requireContext(), PrefsManager.KEY_KB_HEIGHT, 240)} dp"
            textSize = 14f
            setTextColor(Color.parseColor("#B0B0D0"))
        }
        val seek = SeekBar(requireContext()).apply {
            min = 180; max = 340
            progress = PrefsManager.getInt(requireContext(), PrefsManager.KEY_KB_HEIGHT, 240)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, u: Boolean) {
                    label.text = "Height: $p dp"
                    PrefsManager.putInt(requireContext(), PrefsManager.KEY_KB_HEIGHT, p)
                }
                override fun onStartTrackingTouch(p0: SeekBar?) = Unit
                override fun onStopTrackingTouch(p0: SeekBar?) = Unit
            })
        }
        col.addView(label)
        col.addView(seek)
        return col
    }
}
