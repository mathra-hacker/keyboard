package com.mathra.keyboard.dashboard

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.graphics.Color
import androidx.fragment.app.Fragment
import com.mathra.keyboard.util.PrefsManager

/**
 * AiSettingsFragment: enable/disable AI, configure endpoint URL and API key.
 *
 * The API key is stored in EncryptedSharedPreferences.
 * No key is ever logged or shown in plaintext after it is saved.
 */
class AiSettingsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val scroll = ScrollView(requireContext()).apply { setBackgroundColor(Color.parseColor("#0D0D1A")) }
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32)
        }

        // Title
        layout.addView(sectionTitle("🤖  AI Writing Assistant"))

        // Consent notice
        layout.addView(notice("${getString(com.mathra.keyboard.R.string.ai_consent)}\n\n⚠️ Password fields are always excluded from AI requests."))

        // Enable toggle
        layout.addView(switchRow())

        // Endpoint URL
        layout.addView(label("API Endpoint URL"))
        val endpointEdit = editText(
            PrefsManager.getString(requireContext(), PrefsManager.KEY_AI_ENDPOINT,
                "https://api.openai.com/v1/chat/completions"),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        )
        layout.addView(endpointEdit)

        // API Key (password field)
        layout.addView(label("API Key (stored encrypted)"))
        val apiKeyEdit = editText("", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        apiKeyEdit.hint = if (PrefsManager.getSecureString(requireContext(), PrefsManager.KEY_AI_API_KEY).isNotEmpty())
            "●●●●●●●●  (saved)" else "Enter API key..."
        layout.addView(apiKeyEdit)

        // Save button
        val saveBtn = Button(requireContext()).apply {
            text = "Save Settings"
            setBackgroundColor(Color.parseColor("#7B2FBE"))
            setTextColor(Color.WHITE)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 24; layoutParams = p
            setOnClickListener {
                val endpoint = endpointEdit.text.toString().trim()
                val key = apiKeyEdit.text.toString().trim()
                if (endpoint.isNotEmpty()) {
                    PrefsManager.putString(requireContext(), PrefsManager.KEY_AI_ENDPOINT, endpoint)
                }
                if (key.isNotEmpty()) {
                    PrefsManager.putSecureString(requireContext(), PrefsManager.KEY_AI_API_KEY, key)
                    apiKeyEdit.text.clear()
                    apiKeyEdit.hint = "●●●●●●●●  (saved)"
                }
                Toast.makeText(requireContext(), "AI settings saved.", Toast.LENGTH_SHORT).show()
            }
        }
        layout.addView(saveBtn)

        scroll.addView(layout)
        return scroll
    }

    private fun sectionTitle(text: String) = TextView(requireContext()).apply {
        this.text = text; textSize = 20f; setTextColor(Color.parseColor("#00E5FF"))
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.bottomMargin = 16; layoutParams = p
    }

    private fun notice(text: String) = TextView(requireContext()).apply {
        this.text = text; textSize = 13f; setTextColor(Color.parseColor("#B0B0D0"))
        setBackgroundColor(Color.parseColor("#1A1A30"))
        setPadding(24, 16, 24, 16)
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.bottomMargin = 20; layoutParams = p
    }

    private fun switchRow(): View {
        val row = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0, 8, 0, 8) }
        val tv = TextView(requireContext()).apply {
            text = "Enable AI Assistant"; textSize = 15f; setTextColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val sw = Switch(requireContext()).apply {
            isChecked = PrefsManager.getBoolean(requireContext(), PrefsManager.KEY_AI_ENABLED, false)
            setOnCheckedChangeListener { _, c -> PrefsManager.putBoolean(requireContext(), PrefsManager.KEY_AI_ENABLED, c) }
        }
        row.addView(tv); row.addView(sw)
        return row
    }

    private fun label(text: String) = TextView(requireContext()).apply {
        this.text = text; textSize = 13f; setTextColor(Color.parseColor("#88CCFF"))
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.topMargin = 16; p.bottomMargin = 4; layoutParams = p
    }

    private fun editText(initial: String, inputType: Int) = EditText(requireContext()).apply {
        setText(initial); this.inputType = inputType
        setTextColor(Color.WHITE); setHintTextColor(Color.parseColor("#666666"))
        setBackgroundColor(Color.parseColor("#1E1E3A"))
        setPadding(16, 12, 16, 12)
    }
}
