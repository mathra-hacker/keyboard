package com.mathra.keyboard.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import com.mathra.keyboard.BuildConfig

/**
 * AboutActivity: visually stunning About screen.
 *
 * Features:
 *  - Animated neon logo (pulse + glow)
 *  - Gradient background
 *  - Glowing info cards
 *  - Developer info: දෙනුවන් ආකාශ් / 0783756055
 *  - Privacy policy link, open-source licenses, support
 *  - Smooth scroll
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0D0D1A"))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 60, 32, 60)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        // ── Animated Logo ────────────────────────────────────────────────
        val logoText = ImageView(this).apply {
            setImageResource(com.mathra.keyboard.R.drawable.ic_logo)
            scaleType = ImageView.ScaleType.FIT_CENTER
            val size = (140 * resources.displayMetrics.density).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).also {
                it.gravity = Gravity.CENTER_HORIZONTAL
            }
        }
        root.addView(logoText)

        // ── App Name ─────────────────────────────────────────────────────
        root.addView(glowText("MATHRA KEYBOARD", 28f, Color.parseColor("#00E5FF")).also {
            (it.layoutParams as LinearLayout.LayoutParams).topMargin = 24
        })

        // ── Tagline ──────────────────────────────────────────────────────
        root.addView(bodyText("Premium Sinhala + English Keyboard\nfor Sri Lankan Users", 14f).also {
            it.gravity = Gravity.CENTER
            (it.layoutParams as LinearLayout.LayoutParams).topMargin = 8
        })

        // ── Version card ─────────────────────────────────────────────────
        root.addView(glowCard(listOf(
            "Version" to BuildConfig.VERSION_NAME,
            "Build" to BuildConfig.VERSION_CODE.toString(),
            "Platform" to "Android 8.0+"
        )))

        // ── Developer card ───────────────────────────────────────────────
        val devCard = glowCard(listOf(
            "Developer" to "දෙනුවන් ආකාශ්",
            "Contact" to "0783756055"
        ))
        root.addView(devCard)

        // Contact button
        val callBtn = neonButton("📞  Call Developer") {
            try {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:0783756055")))
            } catch (_: Exception) {
                Toast.makeText(this, "Could not open dialer", Toast.LENGTH_SHORT).show()
            }
        }
        root.addView(callBtn)

        // ── Features card ────────────────────────────────────────────────
        root.addView(sectionTitle("✨  Features"))
        root.addView(glowCard(listOf(
            "Languages" to "English + සිංහල + Singlish",
            "Themes" to "5 Premium Themes",
            "Emoji" to "500+ Emoji in 9 Categories",
            "AI Assistant" to "Optional (requires API key)",
            "Privacy" to "No keystroke logging"
        )))

        // ── Open Source section ──────────────────────────────────────────
        root.addView(sectionTitle("📦  Open Source Libraries"))
        root.addView(licenseCard("Retrofit 2", "Square Inc.", "Apache 2.0"))
        root.addView(licenseCard("OkHttp 4", "Square Inc.", "Apache 2.0"))
        root.addView(licenseCard("AndroidX Core / AppCompat / Lifecycle / Navigation", "Google LLC", "Apache 2.0"))
        root.addView(licenseCard("Gson", "Google LLC", "Apache 2.0"))
        root.addView(licenseCard("AndroidX Security Crypto", "Google LLC", "Apache 2.0"))
        root.addView(licenseCard("Material Components", "Google LLC", "Apache 2.0"))

        // ── Privacy ──────────────────────────────────────────────────────
        root.addView(sectionTitle("🔒  Privacy"))
        root.addView(bodyText(
            "Mathra Keyboard does not collect, log or transmit any keystrokes, personal data or private messages.\n\n" +
            "AI requests are sent only when you explicitly tap the AI Assist button. " +
            "Password fields are always excluded.\n\n" +
            "All preferences are stored locally on your device.",
            13f
        ))

        // ── Support ──────────────────────────────────────────────────────
        root.addView(sectionTitle("💬  Support"))
        root.addView(bodyText("For support or feedback, contact the developer:\n0783756055", 14f))

        // ── Footer ───────────────────────────────────────────────────────
        root.addView(bodyText("© 2026 දෙනුවන් ආකාශ් · Mathra Keyboard", 12f).also {
            it.gravity = Gravity.CENTER
            (it.layoutParams as LinearLayout.LayoutParams).topMargin = 40
        })

        scroll.addView(root)
        setContentView(scroll)

        // Animate logo
        animateLogo(logoText)
    }

    // ─── Builders ─────────────────────────────────────────────────────────

    private fun glowText(text: String, size: Float, color: Int): TextView =
        TextView(this).apply {
            this.text = text; textSize = size; setTextColor(color)
            gravity = Gravity.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

    private fun bodyText(text: String, size: Float): TextView =
        TextView(this).apply {
            this.text = text; textSize = size; setTextColor(Color.parseColor("#B0B0D0"))
            setLineSpacing(0f, 1.5f)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 8; layoutParams = p
        }

    private fun sectionTitle(text: String): TextView =
        TextView(this).apply {
            this.text = text; textSize = 18f; setTextColor(Color.parseColor("#00E5FF"))
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 32; p.bottomMargin = 8; layoutParams = p
        }

    private fun glowCard(items: List<Pair<String, String>>): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1A1A3A"))
            setPadding(24, 20, 24, 20)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 20f
                setColor(Color.parseColor("#16163A"))
                setStroke(2, Color.parseColor("#3D2B6B"))
            }
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 12; layoutParams = p
        }
        items.forEach { (k, v) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                p.bottomMargin = 8; layoutParams = p
            }
            row.addView(TextView(this).apply {
                text = k; textSize = 13f; setTextColor(Color.parseColor("#88CCFF"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            row.addView(TextView(this).apply {
                text = v; textSize = 13f; setTextColor(Color.WHITE)
                gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.5f)
            })
            card.addView(row)
        }
        return card
    }

    private fun licenseCard(lib: String, author: String, license: String): View =
        TextView(this).apply {
            text = "• $lib\n  $author · $license"
            textSize = 13f; setTextColor(Color.parseColor("#B0B0D0"))
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            p.topMargin = 6; layoutParams = p
        }

    private fun neonButton(label: String, action: () -> Unit): View =
        Button(this).apply {
            text = label; textSize = 14f; setTextColor(Color.WHITE)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 30f
                colors = intArrayOf(Color.parseColor("#7B2FBE"), Color.parseColor("#00E5FF"))
                gradientType = android.graphics.drawable.GradientDrawable.LINEAR_GRADIENT
                orientation = android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT
            }
            val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                (52 * resources.displayMetrics.density).toInt())
            p.topMargin = 12; layoutParams = p
            setOnClickListener { action() }
        }

    // ─── Logo Animation ───────────────────────────────────────────────────

    private fun animateLogo(view: View) {
        view.alpha = 0f; view.scaleX = 0.5f; view.scaleY = 0.5f
        view.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(700)
            .setInterpolator(android.view.animation.OvershootInterpolator(1.5f))
            .withEndAction { pulseLogo(view) }
            .start()
    }

    private fun pulseLogo(view: View) {
        view.animate().scaleX(1.08f).scaleY(1.08f).setDuration(900)
            .withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(900)
                    .withEndAction { pulseLogo(view) }.start()
            }.start()
    }
}
