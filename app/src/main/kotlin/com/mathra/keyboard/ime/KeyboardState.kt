package com.mathra.keyboard.ime

/**
 * KeyboardState: immutable snapshot of the keyboard's current input state.
 * The IME holds the current state and creates new states on transitions.
 */
data class KeyboardState(
    val inputMode: InputMode = InputMode.ENGLISH,
    val shiftState: ShiftState = ShiftState.OFF,
    val layoutMode: LayoutMode = LayoutMode.LETTERS,
    val singlishBuffer: String = "",       // accumulating Singlish romanized input
    val capsLockOn: Boolean = false,
) {

    /**
     * Input language / mode.
     * ENGLISH   → QWERTY Latin
     * SINHALA   → Sinhala Unicode direct input via Sinhala key layout
     * SINGLISH  → QWERTY Latin input → transliterated to Sinhala in real time
     */
    enum class InputMode { ENGLISH, SINHALA, SINGLISH }

    /**
     * Shift state machine.
     * OFF       → lowercase
     * ON        → uppercase for next character only (single shift)
     * CAPS_LOCK → uppercase until manually toggled off
     */
    enum class ShiftState { OFF, ON, CAPS_LOCK }

    /**
     * Which layout panel is visible.
     */
    enum class LayoutMode { LETTERS, NUMBERS, SYMBOLS, EMOJI }

    // ─── Transition helpers ──────────────────────────────────────────────

    fun withMode(mode: InputMode): KeyboardState =
        copy(inputMode = mode, singlishBuffer = "")

    fun withShift(shift: ShiftState): KeyboardState =
        copy(shiftState = shift, capsLockOn = shift == ShiftState.CAPS_LOCK)

    fun withLayout(layout: LayoutMode): KeyboardState =
        copy(layoutMode = layout)

    fun afterCharTyped(): KeyboardState =
        if (shiftState == ShiftState.ON) copy(shiftState = ShiftState.OFF) else this

    fun appendSinglish(ch: Char): KeyboardState =
        copy(singlishBuffer = singlishBuffer + ch)

    fun clearSinglish(): KeyboardState =
        copy(singlishBuffer = "")

    fun backspaceSinglish(): KeyboardState =
        if (singlishBuffer.isNotEmpty())
            copy(singlishBuffer = singlishBuffer.dropLast(1))
        else this

    val isUpperCase: Boolean
        get() = shiftState != ShiftState.OFF

    val effectiveMode: InputMode
        get() = inputMode

    fun cycleInputMode(): KeyboardState {
        val next = when (inputMode) {
            InputMode.ENGLISH  -> InputMode.SINGLISH
            InputMode.SINGLISH -> InputMode.SINHALA
            InputMode.SINHALA  -> InputMode.ENGLISH
        }
        return withMode(next)
    }
}
