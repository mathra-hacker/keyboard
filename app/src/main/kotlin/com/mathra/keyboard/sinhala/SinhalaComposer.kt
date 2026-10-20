package com.mathra.keyboard.sinhala

/**
 * SinhalaComposer handles Unicode character composition for the Sinhala script.
 *
 * Sinhala writing rules:
 *  - A consonant has an inherent vowel "a" (e.g., ක = ka)
 *  - To write a consonant with a different vowel, attach a vowel sign (e.g., කා = kaa)
 *  - To suppress the inherent vowel, attach hal kirima ් (U+0DCA): ක් = k
 *  - Consonant clusters use ් between consonants: ස්ථ = stha
 *  - Rakaaraansaya: consonant + ZWJ + ර: ක‍ේ = special form
 *  - Yansaya: consonant + ZWJ + ය
 *
 * This composer tracks a "composing buffer" and applies Unicode combining rules
 * correctly before committing text to the InputConnection.
 */
class SinhalaComposer {

    private val composingBuffer = StringBuilder()

    /**
     * Current composition state machine.
     */
    private enum class State {
        EMPTY,
        HAS_CONSONANT,      // last char is a Sinhala consonant
        HAS_HALANT,         // consonant + ් (ready to add another consonant or vowel sign)
        HAS_VOWEL_SIGN,     // consonant + vowel sign (composition complete)
        HAS_VOWEL,          // independent vowel written
    }

    private var state = State.EMPTY

    /** Returns the current composing text (for display in the composing span). */
    val composingText: String get() = composingBuffer.toString()

    /** True when there is uncommitted text in the buffer. */
    val isComposing: Boolean get() = composingBuffer.isNotEmpty()

    /**
     * Process a Sinhala Unicode character and update the composing buffer.
     * @return CompositionResult indicating what to commit (if anything) and new composing text.
     */
    fun processCharacter(ch: Char): CompositionResult {
        return when {
            isConsonant(ch) -> handleConsonant(ch)
            isVowelSign(ch) -> handleVowelSign(ch)
            isIndependentVowel(ch) -> handleIndependentVowel(ch)
            ch == SinhalaConstants.SIGN_AL -> handleHalant()
            ch == SinhalaConstants.ANUSVARA -> handleAnusvara()
            ch == SinhalaConstants.ZWJ -> handleZWJ()
            else -> handleOther(ch)
        }
    }

    /**
     * Commit current buffer and reset. Call when user types space, punctuation or
     * switches to a different character type.
     */
    fun commit(): CompositionResult {
        val text = composingBuffer.toString()
        reset()
        return CompositionResult(commitText = text, newComposing = "")
    }

    /** Backspace: remove last character from buffer. */
    fun backspace(): CompositionResult {
        if (composingBuffer.isEmpty()) {
            return CompositionResult(deleteBack = 1, newComposing = "")
        }
        // Remove last code point (may be a multi-byte char or combining mark)
        val lastIndex = composingBuffer.length - 1
        composingBuffer.deleteCharAt(lastIndex)
        // If previous char is ZWJ, remove it too
        if (composingBuffer.isNotEmpty() && composingBuffer.last() == SinhalaConstants.ZWJ) {
            composingBuffer.deleteCharAt(composingBuffer.length - 1)
        }
        updateState()
        return CompositionResult(newComposing = composingBuffer.toString())
    }

    fun reset() {
        composingBuffer.clear()
        state = State.EMPTY
    }

    // ─── Private Handlers ─────────────────────────────────────────────────

    private fun handleConsonant(ch: Char): CompositionResult {
        return when (state) {
            State.EMPTY, State.HAS_VOWEL -> {
                // Commit previous vowel (if any), start new consonant
                val toCommit = if (composingBuffer.isNotEmpty()) composingBuffer.toString() else ""
                composingBuffer.clear()
                composingBuffer.append(ch)
                state = State.HAS_CONSONANT
                CompositionResult(commitText = toCommit, newComposing = composingBuffer.toString())
            }
            State.HAS_HALANT -> {
                // Consonant cluster: previous_consonant + ් + new_consonant
                composingBuffer.append(ch)
                state = State.HAS_CONSONANT
                CompositionResult(newComposing = composingBuffer.toString())
            }
            State.HAS_CONSONANT, State.HAS_VOWEL_SIGN -> {
                // Commit existing syllable, start new one
                val toCommit = composingBuffer.toString()
                composingBuffer.clear()
                composingBuffer.append(ch)
                state = State.HAS_CONSONANT
                CompositionResult(commitText = toCommit, newComposing = composingBuffer.toString())
            }
        }
    }

    private fun handleVowelSign(sign: Char): CompositionResult {
        return when (state) {
            State.HAS_CONSONANT, State.HAS_HALANT -> {
                // Strip halant if it was pending (vowel sign replaces it)
                if (composingBuffer.endsWith(SinhalaConstants.SIGN_AL)) {
                    composingBuffer.deleteCharAt(composingBuffer.length - 1)
                }
                composingBuffer.append(sign)
                state = State.HAS_VOWEL_SIGN
                CompositionResult(newComposing = composingBuffer.toString())
            }
            State.HAS_VOWEL_SIGN -> {
                // Replace existing vowel sign with new one
                composingBuffer.deleteCharAt(composingBuffer.length - 1)
                composingBuffer.append(sign)
                CompositionResult(newComposing = composingBuffer.toString())
            }
            else -> {
                // Standalone vowel sign — unusual; commit as-is
                val toCommit = composingBuffer.toString()
                composingBuffer.clear()
                composingBuffer.append(sign)
                state = State.HAS_VOWEL_SIGN
                CompositionResult(commitText = toCommit, newComposing = composingBuffer.toString())
            }
        }
    }

    private fun handleIndependentVowel(ch: Char): CompositionResult {
        val toCommit = composingBuffer.toString()
        composingBuffer.clear()
        composingBuffer.append(ch)
        state = State.HAS_VOWEL
        return CompositionResult(commitText = toCommit, newComposing = composingBuffer.toString())
    }

    private fun handleHalant(): CompositionResult {
        return when (state) {
            State.HAS_CONSONANT -> {
                composingBuffer.append(SinhalaConstants.SIGN_AL)
                state = State.HAS_HALANT
                CompositionResult(newComposing = composingBuffer.toString())
            }
            State.HAS_VOWEL_SIGN -> {
                // Remove vowel sign, add halant
                composingBuffer.deleteCharAt(composingBuffer.length - 1)
                composingBuffer.append(SinhalaConstants.SIGN_AL)
                state = State.HAS_HALANT
                CompositionResult(newComposing = composingBuffer.toString())
            }
            else -> {
                CompositionResult(newComposing = composingBuffer.toString())
            }
        }
    }

    private fun handleAnusvara(): CompositionResult {
        // ං can follow a vowel or vowel sign
        val toCommit = composingBuffer.toString()
        composingBuffer.clear()
        composingBuffer.append(SinhalaConstants.ANUSVARA)
        state = State.HAS_VOWEL
        return CompositionResult(commitText = toCommit, newComposing = composingBuffer.toString())
    }

    private fun handleZWJ(): CompositionResult {
        if (state == State.HAS_HALANT && composingBuffer.isNotEmpty()) {
            // ZWJ after halant: Rakaaraansaya / Yansaya preparation
            composingBuffer.append(SinhalaConstants.ZWJ)
            return CompositionResult(newComposing = composingBuffer.toString())
        }
        return CompositionResult(newComposing = composingBuffer.toString())
    }

    private fun handleOther(ch: Char): CompositionResult {
        val toCommit = composingBuffer.toString() + ch
        composingBuffer.clear()
        state = State.EMPTY
        return CompositionResult(commitText = toCommit, newComposing = "")
    }

    // ─── Character classification helpers ────────────────────────────────

    private fun isConsonant(ch: Char): Boolean =
        ch in '\u0D9A'..'\u0DC6'

    private fun isVowelSign(ch: Char): Boolean =
        ch in '\u0DCF'..'\u0DDF' && ch != SinhalaConstants.SIGN_AL

    private fun isIndependentVowel(ch: Char): Boolean =
        ch in '\u0D85'..'\u0D96'

    private fun updateState() {
        state = when {
            composingBuffer.isEmpty() -> State.EMPTY
            isConsonant(composingBuffer.last()) -> State.HAS_CONSONANT
            composingBuffer.last() == SinhalaConstants.SIGN_AL -> State.HAS_HALANT
            isVowelSign(composingBuffer.last()) -> State.HAS_VOWEL_SIGN
            isIndependentVowel(composingBuffer.last()) -> State.HAS_VOWEL
            else -> State.EMPTY
        }
    }

    /**
     * Result of a composition step.
     * @param commitText text to commit to the InputConnection (if not null/empty)
     * @param newComposing the new composing span text to set
     * @param deleteBack number of characters to delete backwards (for backspace on non-composing)
     */
    data class CompositionResult(
        val commitText: String = "",
        val newComposing: String = "",
        val deleteBack: Int = 0,
    )
}
