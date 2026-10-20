package com.mathra.keyboard.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import androidx.fragment.app.Fragment

/**
 * PrivacySettingsFragment: displays the keyboard's privacy policy clearly.
 */
class PrivacySettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scroll = ScrollView(requireContext()).apply { setBackgroundColor(Color.parseColor("#0D0D1A")) }
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32)
        }

        fun title(t: String) = TextView(requireContext()).apply {
            text = t; textSize = 18f; setTextColor(Color.parseColor("#00E5FF"))
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 24; p.bottomMargin = 8; layoutParams = p
        }
        fun body(t: String) = TextView(requireContext()).apply {
            text = t; textSize = 14f; setTextColor(Color.parseColor("#C0C0C0")); setLineSpacing(0f, 1.5f)
        }

        layout.addView(TextView(requireContext()).apply {
            text = "🔒  Privacy Policy"; textSize = 22f; setTextColor(Color.WHITE)
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.bottomMargin = 16; layoutParams = p
        })

        layout.addView(title("What We Collect"))
        layout.addView(body("Mathra Keyboard collects NO typed text. Your keystrokes are processed entirely on your device and are NEVER sent to any server automatically."))

        layout.addView(title("AI Assistant"))
        layout.addView(body("The optional AI writing assistant sends text to an AI API ONLY when YOU explicitly tap the AI Assist button. Password and PIN fields are always excluded. You can disable AI completely in AI Settings."))

        layout.addView(title("Clipboard"))
        layout.addView(body("Clipboard history is stored locally on your device only and is never uploaded. You can clear it at any time from the keyboard."))

        layout.addView(title("Theme & Settings"))
        layout.addView(body("Your theme preferences, sound, vibration and language settings are stored locally in SharedPreferences on your device only."))

        layout.addView(title("Permissions"))
        layout.addView(body("• VIBRATE — for haptic key feedback (optional)\n• INTERNET — only for AI assistant (optional, requires your explicit setup)\n• ACCESS_NETWORK_STATE — to check connectivity before AI calls"))

        layout.addView(title("No Surveillance"))
        layout.addView(body("Mathra Keyboard does not log your typing, does not track your usage patterns, does not contain ads, and does not send analytics."))

        layout.addView(title("Contact"))
        layout.addView(body("Developer: දෙනුවන් ආකාශ්\nContact: 0783756055"))

        layout.addView(title("Last Updated"))
        layout.addView(body("October 2026"))

        scroll.addView(layout)
        return scroll
    }
}
