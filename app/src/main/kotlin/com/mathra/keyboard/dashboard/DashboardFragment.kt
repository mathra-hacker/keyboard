package com.mathra.keyboard.dashboard

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mathra.keyboard.R
import com.mathra.keyboard.about.AboutActivity
import com.mathra.keyboard.databinding.FragmentDashboardBinding

/**
 * DashboardFragment: main screen shown on app launch.
 *
 * Shows:
 *  - Animated Mathra logo
 *  - IME status (enabled? selected as default?)
 *  - Enable Keyboard button (opens IME settings)
 *  - Select Keyboard button (opens input method chooser)
 *  - Navigation buttons to all settings screens
 */
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
        startLogoAnimation()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    // ─── Status detection ─────────────────────────────────────────────────

    private fun updateStatus() {
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        val enabled = isKeyboardEnabled(imm)
        val selected = isKeyboardSelected(imm)

        binding.tvEnableStatus.text = if (enabled)
            getString(R.string.keyboard_status_enabled)
        else
            getString(R.string.keyboard_status_disabled)
        binding.tvEnableStatus.setTextColor(
            if (enabled) resources.getColor(R.color.status_ok, null)
            else resources.getColor(R.color.status_warn, null)
        )

        binding.tvSelectStatus.text = if (selected)
            getString(R.string.keyboard_status_selected)
        else
            getString(R.string.keyboard_status_not_selected)
        binding.tvSelectStatus.setTextColor(
            if (selected) resources.getColor(R.color.status_ok, null)
            else resources.getColor(R.color.status_warn, null)
        )
    }

    private fun isKeyboardEnabled(imm: InputMethodManager): Boolean {
        return imm.enabledInputMethodList.any {
            it.packageName == requireContext().packageName
        }
    }

    private fun isKeyboardSelected(imm: InputMethodManager): Boolean {
        val current = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        return current?.contains(requireContext().packageName) == true
    }

    // ─── Button setup ─────────────────────────────────────────────────────

    private fun setupButtons() {
        binding.btnEnableKeyboard.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        binding.btnSelectKeyboard.setOnClickListener {
            val imm = requireContext().getSystemService(InputMethodManager::class.java)
            imm.showInputMethodPicker()
        }
        binding.btnThemes.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_themeGallery)
        }
        binding.btnLanguage.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_languageSettings)
        }
        binding.btnEmojiSettings.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_emojiSettings)
        }
        binding.btnAiSettings.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_aiSettings)
        }
        binding.btnPrivacy.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_privacySettings)
        }
        binding.btnAbout.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }
    }

    // ─── Logo animation ───────────────────────────────────────────────────

    private fun startLogoAnimation() {
        binding.ivLogo.alpha = 0f
        binding.ivLogo.scaleX = 0.7f
        binding.ivLogo.scaleY = 0.7f
        binding.ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .setInterpolator(android.view.animation.OvershootInterpolator())
            .start()

        // Glow pulse animation
        binding.ivLogo.postDelayed({
            binding.ivLogo.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(800)
                .withEndAction {
                    binding.ivLogo.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(800)
                        .start()
                }
                .start()
        }, 700)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
