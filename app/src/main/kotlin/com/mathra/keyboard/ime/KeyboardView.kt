package com.mathra.keyboard.ime

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.ColorUtils
import com.mathra.keyboard.theme.KeyboardTheme
import com.mathra.keyboard.util.SoundHelper
import com.mathra.keyboard.util.VibrationHelper
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * KeyboardView: fully custom Canvas-drawn keyboard.
 *
 * Key layout is computed dynamically based on view width.
 * Supports:
 *   - QWERTY English / Sinhala / Singlish letter rows
 *   - Number row, symbol row
 *   - Shift, CapsLock, Delete, Space, Enter, Lang, Emoji keys
 *   - Long-press on keys for alternates
 *   - Scale-down press animation with glow
 *   - Ripple-like highlight on touch
 *   - Theme-driven colors
 */
class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // ─── Listener ─────────────────────────────────────────────────────────

    interface KeyboardActionListener {
        fun onKey(key: Key)
        fun onLongPress(key: Key)
    }

    var listener: KeyboardActionListener? = null

    // ─── Theme ────────────────────────────────────────────────────────────

    var theme: KeyboardTheme = KeyboardTheme.DarkNeon
        set(value) { field = value; invalidate() }

    // ─── State ────────────────────────────────────────────────────────────

    var keyboardState: KeyboardState = KeyboardState()
        set(value) { field = value; buildLayout(); invalidate() }

    // ─── Layout ───────────────────────────────────────────────────────────

    private var keys: List<Key> = emptyList()
    private var pressedKey: Key? = null
    private var pressedTime = 0L
    private var longPressHandled = false
    private val LONG_PRESS_MS = 400L

    // Key geometry
    private var keyHeight = 0f
    private var keyGap = 5f

    // ─── Paint objects ────────────────────────────────────────────────────

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
    }

    // Animation scale (0.9..1.0) for pressed key
    private val keyScales = mutableMapOf<Key, Float>()

    // ─── Typeface ─────────────────────────────────────────────────────────

    private val keyTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    private val sinhalaTypeface = Typeface.DEFAULT

    // ─── Key data model ───────────────────────────────────────────────────

    data class Key(
        val code: KeyCode,
        val label: String,
        val subLabel: String = "",
        val type: KeyType = KeyType.NORMAL,
        // Geometry (set during layout)
        var x: Float = 0f,
        var y: Float = 0f,
        var width: Float = 0f,
        var height: Float = 0f,
    ) {
        fun contains(px: Float, py: Float) =
            px >= x && px <= x + width && py >= y && py <= y + height

        val bounds: RectF get() = RectF(x, y, x + width, y + height)
    }

    enum class KeyType { NORMAL, SPECIAL, SPACE, ENTER, SHIFT, DELETE, LANG, EMOJI, NUM }

    sealed class KeyCode {
        data class Char(val ch: kotlin.Char) : KeyCode()
        data class SinhalaChar(val unicode: kotlin.Char) : KeyCode()
        object Space     : KeyCode()
        object Enter     : KeyCode()
        object Delete    : KeyCode()
        object Shift     : KeyCode()
        object CapsLock  : KeyCode()
        object Lang      : KeyCode()
        object Emoji     : KeyCode()
        object Numbers   : KeyCode()
        object Letters   : KeyCode()
        object Symbols   : KeyCode()
        object ArrowLeft : KeyCode()
        object ArrowRight: KeyCode()
        data class SinglishChar(val ch: kotlin.Char) : KeyCode()
    }

    // ─── QWERTY rows ──────────────────────────────────────────────────────

    private val qwertyRows = listOf(
        listOf("q","w","e","r","t","y","u","i","o","p"),
        listOf("a","s","d","f","g","h","j","k","l"),
        listOf("SHIFT","z","x","c","v","b","n","m","DELETE"),
        listOf("LANG","123","EMOJI","SPACE","ENTER"),
    )

    private val numberRows = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0"),
        listOf("-","/",":",";","(",")","\$","&","@","\""),
        listOf("SYMBOLS",".","," ,  "?","!","'","DELETE"),
        listOf("ABC","SPACE","ENTER"),
    )

    private val symbolRows = listOf(
        listOf("[","]","{","}","#","%","^","*","+","="),
        listOf("_","\\","|","~","<",">","€","£","¥","•"),
        listOf("ABC",".","," ,  "?","!","'","DELETE"),
        listOf("123","SPACE","ENTER"),
    )

    // Sinhala Unicode direct rows (based on common Sinhala layout)
    private val sinhalaRows = listOf(
        listOf("ක","ල","ද","ත","ම","ස","ශ","ය","ව","ේ"),
        listOf("ෙ","ු","ා","ෑ","ෛ","ෝ","ෞ","ි","ී","ැ"),
        listOf("SHIFT","ග","ජ","ෘ","ං","ට","ර","BACKSPACE"),
        listOf("LANG","123","EMOJI","SPACE","ENTER"),
    )

    // ─── Layout computation ───────────────────────────────────────────────

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        buildLayout()
    }

    private fun buildLayout() {
        if (width == 0 || height == 0) return
        keys = when (keyboardState.layoutMode) {
            KeyboardState.LayoutMode.LETTERS -> buildLetterLayout()
            KeyboardState.LayoutMode.NUMBERS -> buildNumberLayout()
            KeyboardState.LayoutMode.SYMBOLS -> buildSymbolLayout()
            KeyboardState.LayoutMode.EMOJI   -> emptyList() // emoji handled by overlay
        }
    }

    private fun buildLetterLayout(): List<Key> {
        val rows = when (keyboardState.inputMode) {
            KeyboardState.InputMode.SINHALA -> sinhalaRows
            else -> qwertyRows
        }
        return buildGenericLayout(rows)
    }

    private fun buildNumberLayout(): List<Key> = buildGenericLayout(numberRows)
    private fun buildSymbolLayout(): List<Key> = buildGenericLayout(symbolRows)

    private fun buildGenericLayout(rows: List<List<String>>): List<Key> {
        val result = mutableListOf<Key>()
        val totalH = height.toFloat()
        val totalW = width.toFloat()
        val rowCount = rows.size
        keyGap = 5f * resources.displayMetrics.density
        keyHeight = (totalH - keyGap * (rowCount + 1)) / rowCount
        val cornerR = 10f * resources.displayMetrics.density

        var yPos = keyGap
        for (row in rows) {
            // Compute effective weights
            val weights = row.map { keyWeight(it) }
            val totalWeight = weights.sum()
            val availW = totalW - keyGap * (row.size + 1)
            val unitW = availW / totalWeight

            var xPos = keyGap
            for ((idx, label) in row.withIndex()) {
                val kw = unitW * weights[idx]
                val key = Key(
                    code = labelToCode(label, keyboardState),
                    label = displayLabel(label, keyboardState),
                    subLabel = subLabel(label),
                    type = keyType(label),
                    x = xPos, y = yPos,
                    width = kw, height = keyHeight
                )
                result.add(key)
                xPos += kw + keyGap
            }
            yPos += keyHeight + keyGap
        }
        return result
    }

    private fun keyWeight(label: String): Float = when (label) {
        "SPACE"  -> 3.5f
        "SHIFT", "DELETE", "BACKSPACE" -> 1.5f
        "ENTER"  -> 1.5f
        "LANG", "EMOJI", "123", "ABC", "SYMBOLS" -> 1.2f
        else     -> 1.0f
    }

    private fun keyType(label: String): KeyType = when (label) {
        "SPACE"  -> KeyType.SPACE
        "ENTER"  -> KeyType.ENTER
        "SHIFT"  -> KeyType.SHIFT
        "DELETE", "BACKSPACE" -> KeyType.DELETE
        "LANG"   -> KeyType.LANG
        "EMOJI"  -> KeyType.EMOJI
        "123", "ABC", "SYMBOLS" -> KeyType.NUM
        else     -> KeyType.NORMAL
    }

    private fun displayLabel(label: String, state: KeyboardState): String = when (label) {
        "SHIFT"    -> if (state.shiftState == KeyboardState.ShiftState.CAPS_LOCK) "⇪" else "⇧"
        "DELETE", "BACKSPACE" -> "⌫"
        "SPACE"    -> when (state.inputMode) {
            KeyboardState.InputMode.SINHALA  -> "සිංහල"
            KeyboardState.InputMode.SINGLISH -> "Singlish"
            else -> "Space"
        }
        "ENTER"    -> "↵"
        "LANG"     -> "🌐"
        "EMOJI"    -> "😊"
        "123"      -> "123"
        "ABC"      -> "ABC"
        "SYMBOLS"  -> "#+=\n"
        else -> if (state.isUpperCase) label.uppercase() else label.lowercase()
    }

    private fun subLabel(label: String): String = when (label) {
        "2" -> "ABC"; "3" -> "DEF"; "4" -> "GHI"; "5" -> "JKL"
        "6" -> "MNO"; "7" -> "PQRS"; "8" -> "TUV"; "9" -> "WXYZ"
        else -> ""
    }

    private fun labelToCode(label: String, state: KeyboardState): KeyCode = when (label) {
        "SPACE"    -> KeyCode.Space
        "ENTER"    -> KeyCode.Enter
        "SHIFT"    -> KeyCode.Shift
        "DELETE", "BACKSPACE" -> KeyCode.Delete
        "LANG"     -> KeyCode.Lang
        "EMOJI"    -> KeyCode.Emoji
        "123"      -> KeyCode.Numbers
        "ABC"      -> KeyCode.Letters
        "SYMBOLS"  -> KeyCode.Symbols
        else -> {
            val ch = if (state.isUpperCase) label.uppercase().firstOrNull()
                     else label.lowercase().firstOrNull()
            if (ch != null) {
                when (state.inputMode) {
                    KeyboardState.InputMode.SINHALA  -> KeyCode.SinhalaChar(label.first())
                    KeyboardState.InputMode.SINGLISH -> KeyCode.SinglishChar(ch)
                    else -> KeyCode.Char(ch)
                }
            } else KeyCode.Char(' ')
        }
    }

    // ─── Drawing ──────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(theme.keyboardBg)
        for (key in keys) drawKey(canvas, key)
    }

    private fun drawKey(canvas: Canvas, key: Key) {
        val scale = keyScales[key] ?: 1f
        val isPressed = key == pressedKey

        // Compute scaled rect around centre
        val cx = key.x + key.width / 2
        val cy = key.y + key.height / 2
        val scaledW = key.width * scale
        val scaledH = key.height * scale
        val rect = RectF(cx - scaledW / 2, cy - scaledH / 2, cx + scaledW / 2, cy + scaledH / 2)
        val cornerR = 10f * resources.displayMetrics.density

        // ── Key background ──────────────────────────────────────────────
        keyPaint.style = Paint.Style.FILL
        keyPaint.color = when {
            isPressed -> theme.keyPressed
            key.type == KeyType.SPECIAL ||
            key.type == KeyType.SHIFT ||
            key.type == KeyType.DELETE ||
            key.type == KeyType.LANG ||
            key.type == KeyType.NUM  -> theme.specialKeyBg
            key.type == KeyType.SPACE -> theme.spaceBg
            key.type == KeyType.ENTER -> theme.enterStart  // will be overridden by gradient
            else -> theme.keyBg
        }

        if (key.type == KeyType.ENTER) {
            drawGradientKey(canvas, rect, cornerR, theme.enterStart, theme.enterEnd)
        } else {
            canvas.drawRoundRect(rect, cornerR, cornerR, keyPaint)
        }

        // ── Border ──────────────────────────────────────────────────────
        borderPaint.color = theme.keyBorder
        borderPaint.alpha = if (isPressed) 255 else 120
        canvas.drawRoundRect(rect, cornerR, cornerR, borderPaint)

        // ── Glow on press ───────────────────────────────────────────────
        if (isPressed) {
            glowPaint.color = theme.glowColor
            glowPaint.alpha = 180
            glowPaint.strokeWidth = 3f * resources.displayMetrics.density
            canvas.drawRoundRect(rect, cornerR, cornerR, glowPaint)
        }

        // ── Main label ──────────────────────────────────────────────────
        val isSinhala = key.type == KeyType.NORMAL &&
            keyboardState.inputMode == KeyboardState.InputMode.SINHALA
        textPaint.typeface = if (isSinhala) sinhalaTypeface else keyTypeface
        textPaint.textSize = when (key.type) {
            KeyType.SPACE, KeyType.ENTER, KeyType.NUM -> 13f * resources.displayMetrics.density
            KeyType.DELETE, KeyType.SHIFT -> 16f * resources.displayMetrics.density
            else -> 16f * resources.displayMetrics.density
        }
        textPaint.color = when (key.type) {
            KeyType.ENTER  -> Color.WHITE
            KeyType.SPECIAL, KeyType.DELETE, KeyType.SHIFT,
            KeyType.LANG, KeyType.NUM -> theme.specialKeyText
            KeyType.SPACE  -> ColorUtils.blendARGB(theme.keyText, theme.accentColor, 0.3f)
            else -> theme.keyText
        }

        val textY = rect.centerY() + textPaint.textSize * 0.35f
        canvas.drawText(key.label, rect.centerX(), textY, textPaint)

        // ── Sub-label (top-right corner) ────────────────────────────────
        if (key.subLabel.isNotEmpty()) {
            subTextPaint.typeface = Typeface.DEFAULT
            subTextPaint.textSize = 9f * resources.displayMetrics.density
            subTextPaint.color = theme.keySubText
            canvas.drawText(
                key.subLabel,
                rect.right - 8f * resources.displayMetrics.density,
                rect.top + 14f * resources.displayMetrics.density,
                subTextPaint
            )
        }
    }

    private fun drawGradientKey(canvas: Canvas, rect: RectF, r: Float, startColor: Int, endColor: Int) {
        val gradient = LinearGradient(
            rect.left, rect.top, rect.right, rect.bottom,
            startColor, endColor, Shader.TileMode.CLAMP
        )
        keyPaint.shader = gradient
        canvas.drawRoundRect(rect, r, r, keyPaint)
        keyPaint.shader = null
    }

    // ─── Touch handling ───────────────────────────────────────────────────

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val px = event.x
        val py = event.y

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val key = findKey(px, py)
                if (key != null) {
                    pressedKey = key
                    pressedTime = System.currentTimeMillis()
                    longPressHandled = false
                    animatePress(key, 0.87f)

                    // Sound + vibration
                    VibrationHelper.vibrate(context)
                    when (key.type) {
                        KeyType.DELETE -> SoundHelper.playDelete(context)
                        KeyType.ENTER  -> SoundHelper.playReturn(context)
                        KeyType.SPACE  -> SoundHelper.playSpacebar(context)
                        else           -> SoundHelper.playKeyClick(context)
                    }

                    // Schedule long press check
                    postDelayed({
                        if (pressedKey == key && !longPressHandled) {
                            longPressHandled = true
                            listener?.onLongPress(key)
                        }
                    }, LONG_PRESS_MS)

                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val key = pressedKey
                if (key != null) {
                    animateRelease(key)
                    if (!longPressHandled) {
                        listener?.onKey(key)
                    }
                    pressedKey = null
                    invalidate()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun findKey(x: Float, y: Float): Key? =
        keys.firstOrNull { it.contains(x, y) }

    // ─── Press animations ─────────────────────────────────────────────────

    private fun animatePress(key: Key, targetScale: Float) {
        ValueAnimator.ofFloat(1f, targetScale).apply {
            duration = 80
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                keyScales[key] = anim.animatedValue as Float
                invalidate()
            }
        }.start()
    }

    private fun animateRelease(key: Key) {
        val startScale = keyScales[key] ?: 1f
        ValueAnimator.ofFloat(startScale, 1f).apply {
            duration = 100
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                keyScales[key] = anim.animatedValue as Float
                invalidate()
            }
        }.start()
    }

    // ─── Measurement ─────────────────────────────────────────────────────

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val desiredH = (240 * resources.displayMetrics.density).toInt()
        setMeasuredDimension(w, desiredH)
    }
}
