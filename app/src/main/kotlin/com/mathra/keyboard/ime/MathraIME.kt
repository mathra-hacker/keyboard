package com.mathra.keyboard.ime

import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mathra.keyboard.emoji.EmojiPicker
import com.mathra.keyboard.sinhala.SinhalaComposer
import com.mathra.keyboard.sinhala.SuggestionEngine
import com.mathra.keyboard.theme.ThemeManager
import com.mathra.keyboard.util.PrefsManager

/**
 * MathraIME: the main InputMethodService.
 *
 * Responsibilities:
 *  - Create and manage the keyboard view hierarchy
 *  - Route key events to the appropriate handler (English / Sinhala / Singlish)
 *  - Manage the suggestion bar
 *  - Handle shift, caps lock, backspace, enter, space
 *  - Handle language switching between English, Sinhala, Singlish
 *  - Trigger emoji picker overlay
 *  - Correctly handle password/PIN fields (no AI, no suggestions)
 */
class MathraIME : InputMethodService(), KeyboardView.KeyboardActionListener {

    // ─── Views ────────────────────────────────────────────────────────────

    private lateinit var keyboardContainer: LinearLayout
    private lateinit var suggestionBar: RecyclerView
    private lateinit var keyboardView: KeyboardView
    private var emojiPicker: EmojiPicker? = null

    // ─── State ────────────────────────────────────────────────────────────

    private var state = KeyboardState()
    private val sinhalaComposer = SinhalaComposer()
    private val suggestionEngine = SuggestionEngine()
    private val clipboardManager by lazy { ClipboardManager(this) }

    // Singlish accumulator (the raw Roman letters typed so far)
    private var singlishBuffer = StringBuilder()

    // Whether the current field is password/PIN — disables AI & some features
    private var isPasswordField = false

    // ─── IME Lifecycle ────────────────────────────────────────────────────

    override fun onCreateInputView(): View {
        val theme = ThemeManager.getCurrentTheme(this)

        keyboardContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(theme.keyboardBg)
        }

        // Suggestion bar
        suggestionBar = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MathraIME, LinearLayoutManager.HORIZONTAL, false)
            setBackgroundColor(theme.suggestionBg)
        }
        val suggAdapter = SuggestionAdapter(theme) { suggestion ->
            onSuggestionTapped(suggestion)
        }
        suggestionBar.adapter = suggAdapter
        keyboardContainer.addView(
            suggestionBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (44 * resources.displayMetrics.density).toInt()
            )
        )

        // Keyboard view
        keyboardView = KeyboardView(this).apply {
            this.theme = theme
            keyboardState = state
            listener = this@MathraIME
        }
        keyboardContainer.addView(
            keyboardView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        // Load saved input mode
        val savedMode = PrefsManager.getString(this, PrefsManager.KEY_INPUT_MODE, "en")
        state = state.withMode(
            when (savedMode) {
                "si"       -> KeyboardState.InputMode.SINHALA
                "singlish" -> KeyboardState.InputMode.SINGLISH
                else       -> KeyboardState.InputMode.ENGLISH
            }
        )
        applyState()

        return keyboardContainer
    }

    override fun onStartInputView(info: EditorInfo, restarting: Boolean) {
        super.onStartInputView(info, restarting)

        // Detect password fields
        val inputClass = info.inputType and InputType.TYPE_MASK_CLASS
        val inputVariation = info.inputType and InputType.TYPE_MASK_VARIATION
        isPasswordField = inputClass == InputType.TYPE_CLASS_TEXT &&
            (inputVariation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
             inputVariation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
             inputVariation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)

        // Reset composer on new input session
        sinhalaComposer.reset()
        singlishBuffer.clear()
        updateSuggestions(emptyList())
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        commitAnything()
    }

    // ─── Key Actions ─────────────────────────────────────────────────────

    override fun onKey(key: KeyboardView.Key) {
        when (key.code) {
            is KeyboardView.KeyCode.Char        -> handleCharKey(key.code.ch)
            is KeyboardView.KeyCode.SinhalaChar -> handleSinhalaKey(key.code.unicode)
            is KeyboardView.KeyCode.SinglishChar-> handleSinglishKey(key.code.ch)
            KeyboardView.KeyCode.Space     -> handleSpace()
            KeyboardView.KeyCode.Enter     -> handleEnter()
            KeyboardView.KeyCode.Delete    -> handleDelete()
            KeyboardView.KeyCode.Shift     -> handleShift()
            KeyboardView.KeyCode.Lang      -> handleLangSwitch()
            KeyboardView.KeyCode.Emoji     -> handleEmoji()
            KeyboardView.KeyCode.Numbers   -> switchLayout(KeyboardState.LayoutMode.NUMBERS)
            KeyboardView.KeyCode.Letters   -> switchLayout(KeyboardState.LayoutMode.LETTERS)
            KeyboardView.KeyCode.Symbols   -> switchLayout(KeyboardState.LayoutMode.SYMBOLS)
            KeyboardView.KeyCode.CapsLock  -> handleCapsLock()
            KeyboardView.KeyCode.ArrowLeft -> moveCursor(-1)
            KeyboardView.KeyCode.ArrowRight-> moveCursor(1)
            else -> Unit
        }
    }

    override fun onLongPress(key: KeyboardView.Key) {
        when (key.code) {
            KeyboardView.KeyCode.Delete -> {
                // Long-press delete: delete entire word
                deleteWord()
            }
            KeyboardView.KeyCode.Shift -> {
                // Long-press shift: toggle caps lock
                handleCapsLock()
            }
            KeyboardView.KeyCode.Space -> {
                // Long-press space: show language picker
                handleLangSwitch()
            }
            else -> Unit
        }
    }

    // ─── Character input handlers ─────────────────────────────────────────

    private fun handleCharKey(ch: Char) {
        val ic = currentInputConnection ?: return
        commitAnything()
        val out = if (state.isUpperCase) ch.uppercaseChar() else ch.lowercaseChar()
        ic.commitText(out.toString(), 1)
        state = state.afterCharTyped()
        updateSuggestions(
            if (!isPasswordField) suggestionEngine.getEnglishCompletions(getWordBeforeCursor())
            else emptyList()
        )
        applyState()
    }

    private fun handleSinhalaKey(unicode: Char) {
        val ic = currentInputConnection ?: return
        val result = sinhalaComposer.processCharacter(unicode)
        applyCompositionResult(result)
    }

    private fun handleSinglishKey(ch: Char) {
        val ic = currentInputConnection ?: return
        singlishBuffer.append(ch)
        val input = singlishBuffer.toString()

        // Real-time partial transliteration shown in composing span
        val result = com.mathra.keyboard.sinhala.SinglishTransliterator()
        val partial = result.transliteratePartial(input)
        ic.setComposingText(partial, 1)

        // Update suggestion candidates
        if (!isPasswordField) {
            val candidates = suggestionEngine.getSinglishCandidates(input)
            updateSuggestions(candidates)
        }
    }

    // ─── Special keys ─────────────────────────────────────────────────────

    private fun handleSpace() {
        val ic = currentInputConnection ?: return
        when (state.inputMode) {
            KeyboardState.InputMode.SINGLISH -> {
                // Commit transliteration on space
                if (singlishBuffer.isNotEmpty()) {
                    val result = com.mathra.keyboard.sinhala.SinglishTransliterator()
                        .transliterate(singlishBuffer.toString())
                    ic.finishComposingText()
                    ic.commitText(result.primary + " ", 1)
                    singlishBuffer.clear()
                    updateSuggestions(emptyList())
                } else {
                    ic.commitText(" ", 1)
                }
            }
            KeyboardState.InputMode.SINHALA -> {
                val result = sinhalaComposer.commit()
                applyCompositionResult(result)
                ic.commitText(" ", 1)
            }
            else -> {
                commitAnything()
                ic.commitText(" ", 1)
            }
        }
        state = state.afterCharTyped()
        applyState()
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        commitAnything()
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        if (action != null && action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            ic.performEditorAction(action)
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
    }

    private fun handleDelete() {
        val ic = currentInputConnection ?: return
        when {
            state.inputMode == KeyboardState.InputMode.SINGLISH && singlishBuffer.isNotEmpty() -> {
                singlishBuffer.deleteCharAt(singlishBuffer.length - 1)
                if (singlishBuffer.isEmpty()) {
                    ic.finishComposingText()
                    ic.deleteSurroundingText(1, 0)
                    updateSuggestions(emptyList())
                } else {
                    val partial = com.mathra.keyboard.sinhala.SinglishTransliterator()
                        .transliteratePartial(singlishBuffer.toString())
                    ic.setComposingText(partial, 1)
                    updateSuggestions(
                        if (!isPasswordField) suggestionEngine.getSinglishCandidates(singlishBuffer.toString())
                        else emptyList()
                    )
                }
            }
            state.inputMode == KeyboardState.InputMode.SINHALA && sinhalaComposer.isComposing -> {
                val result = sinhalaComposer.backspace()
                applyCompositionResult(result)
            }
            else -> {
                ic.deleteSurroundingText(1, 0)
            }
        }
    }

    private fun handleShift() {
        state = when (state.shiftState) {
            KeyboardState.ShiftState.OFF       -> state.withShift(KeyboardState.ShiftState.ON)
            KeyboardState.ShiftState.ON        -> state.withShift(KeyboardState.ShiftState.OFF)
            KeyboardState.ShiftState.CAPS_LOCK -> state.withShift(KeyboardState.ShiftState.OFF)
        }
        applyState()
    }

    private fun handleCapsLock() {
        state = when (state.shiftState) {
            KeyboardState.ShiftState.CAPS_LOCK -> state.withShift(KeyboardState.ShiftState.OFF)
            else -> state.withShift(KeyboardState.ShiftState.CAPS_LOCK)
        }
        applyState()
    }

    private fun handleLangSwitch() {
        commitAnything()
        sinhalaComposer.reset()
        singlishBuffer.clear()
        state = state.cycleInputMode()
        PrefsManager.putString(this, PrefsManager.KEY_INPUT_MODE, when (state.inputMode) {
            KeyboardState.InputMode.SINHALA  -> "si"
            KeyboardState.InputMode.SINGLISH -> "singlish"
            else -> "en"
        })
        updateSuggestions(emptyList())
        applyState()
    }

    private fun handleEmoji() {
        showEmojiPicker()
    }

    private fun switchLayout(mode: KeyboardState.LayoutMode) {
        state = state.withLayout(mode)
        applyState()
    }

    // ─── Cursor movement ─────────────────────────────────────────────────

    private fun moveCursor(direction: Int) {
        val ic = currentInputConnection ?: return
        ic.sendKeyEvent(KeyEvent(
            KeyEvent.ACTION_DOWN,
            if (direction < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        ))
        ic.sendKeyEvent(KeyEvent(
            KeyEvent.ACTION_UP,
            if (direction < 0) KeyEvent.KEYCODE_DPAD_LEFT else KeyEvent.KEYCODE_DPAD_RIGHT
        ))
    }

    private fun deleteWord() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(50, 0)?.toString() ?: return
        val trimmed = before.trimEnd()
        val spaceIdx = trimmed.lastIndexOf(' ')
        val toDelete = if (spaceIdx >= 0) trimmed.length - spaceIdx else trimmed.length
        if (toDelete > 0) ic.deleteSurroundingText(toDelete, 0)
    }

    // ─── Emoji Picker ─────────────────────────────────────────────────────

    private fun showEmojiPicker() {
        if (emojiPicker == null) {
            emojiPicker = EmojiPicker(this) { emoji ->
                val ic = currentInputConnection ?: return@EmojiPicker
                ic.commitText(emoji, 1)
            }
        }
        // Replace keyboard view with emoji picker temporarily
        if (keyboardContainer.childCount > 1) {
            val last = keyboardContainer.getChildAt(keyboardContainer.childCount - 1)
            if (last is EmojiPicker) {
                // Already showing — dismiss
                keyboardContainer.removeView(last)
                emojiPicker = null
                return
            }
        }
        keyboardContainer.addView(
            emojiPicker,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (280 * resources.displayMetrics.density).toInt()
            )
        )
    }

    // ─── Suggestion handling ──────────────────────────────────────────────

    private fun updateSuggestions(suggestions: List<String>) {
        (suggestionBar.adapter as? SuggestionAdapter)?.update(suggestions)
    }

    private fun onSuggestionTapped(suggestion: String) {
        val ic = currentInputConnection ?: return
        when (state.inputMode) {
            KeyboardState.InputMode.SINGLISH -> {
                ic.finishComposingText()
                // Delete partial singlish-typed chars
                if (singlishBuffer.isNotEmpty()) {
                    ic.deleteSurroundingText(
                        com.mathra.keyboard.sinhala.SinglishTransliterator()
                            .transliteratePartial(singlishBuffer.toString()).length, 0
                    )
                }
                ic.commitText(suggestion + " ", 1)
                singlishBuffer.clear()
            }
            else -> {
                // Replace word before cursor with suggestion
                val before = getWordBeforeCursor()
                if (before.isNotEmpty()) {
                    ic.deleteSurroundingText(before.length, 0)
                }
                ic.commitText(suggestion + " ", 1)
            }
        }
        updateSuggestions(emptyList())
    }

    // ─── Composition helpers ──────────────────────────────────────────────

    private fun applyCompositionResult(result: SinhalaComposer.CompositionResult) {
        val ic = currentInputConnection ?: return
        if (result.deleteBack > 0) {
            ic.finishComposingText()
            ic.deleteSurroundingText(result.deleteBack, 0)
        }
        if (result.commitText.isNotEmpty()) {
            ic.finishComposingText()
            ic.commitText(result.commitText, 1)
        }
        if (result.newComposing.isNotEmpty()) {
            ic.setComposingText(result.newComposing, 1)
        } else {
            ic.finishComposingText()
        }
    }

    private fun commitAnything() {
        val ic = currentInputConnection ?: return
        when (state.inputMode) {
            KeyboardState.InputMode.SINGLISH -> {
                if (singlishBuffer.isNotEmpty()) {
                    val r = com.mathra.keyboard.sinhala.SinglishTransliterator()
                        .transliterate(singlishBuffer.toString())
                    ic.finishComposingText()
                    ic.commitText(r.primary, 1)
                    singlishBuffer.clear()
                }
            }
            KeyboardState.InputMode.SINHALA -> {
                if (sinhalaComposer.isComposing) {
                    ic.finishComposingText()
                    ic.commitText(sinhalaComposer.composingText, 1)
                    sinhalaComposer.reset()
                }
            }
            else -> ic.finishComposingText()
        }
    }

    private fun getWordBeforeCursor(): String {
        val ic = currentInputConnection ?: return ""
        val text = ic.getTextBeforeCursor(50, 0)?.toString() ?: return ""
        val spaceIdx = text.lastIndexOf(' ')
        return if (spaceIdx >= 0) text.substring(spaceIdx + 1) else text
    }

    // ─── Apply state to views ─────────────────────────────────────────────

    private fun applyState() {
        if (::keyboardView.isInitialized) {
            keyboardView.keyboardState = state
            keyboardView.invalidate()
        }
    }
}

// ─── SuggestionAdapter ───────────────────────────────────────────────────────

class SuggestionAdapter(
    private val theme: com.mathra.keyboard.theme.KeyboardTheme,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<SuggestionAdapter.VH>() {

    private var items = listOf<String>()

    fun update(newItems: List<String>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            setPadding(32, 0, 32, 0)
            textSize = 14f
            setTextColor(theme.suggestionText)
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.WRAP_CONTENT,
                RecyclerView.LayoutParams.MATCH_PARENT
            )
            includeFontPadding = false
        }
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        (holder.itemView as TextView).text = item
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    class VH(v: View) : RecyclerView.ViewHolder(v)
}
