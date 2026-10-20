package com.mathra.keyboard.sinhala

/**
 * SinglishTransliterator converts Romanized Sinhala (Singlish) text
 * to Unicode Sinhala, syllable by syllable.
 *
 * Algorithm:
 *  1. Check if the whole input matches a word shortcut (highest priority).
 *  2. If not, greedily match left-to-right against the rules table,
 *     preferring the longest matching prefix.
 *  3. After each match, check if the next rule is a vowel — if so,
 *     convert the previous consonant + vowel to a proper consonant+vowel-sign combination.
 *  4. Return the best single result, plus a list of alternative candidates
 *     for disambiguation.
 *
 * This engine is intentionally stateless and pure. State is kept in the IME.
 */
class SinglishTransliterator {

    // Sorted rules: longer keys first for greedy matching
    private val sortedRules: List<Pair<String, String>> = SinglishRules.rules.entries
        .sortedByDescending { it.key.length }
        .map { it.key to it.value }

    /**
     * Transliterate a full Singlish word to Sinhala.
     * @return TransliterationResult with primary output and alternatives.
     */
    fun transliterate(input: String): TransliterationResult {
        if (input.isBlank()) return TransliterationResult("", emptyList())

        val lower = input.lowercase()

        // 1. Check word shortcuts first
        SinglishRules.wordShortcuts[lower]?.let { shortcut ->
            return TransliterationResult(shortcut, listOf(shortcut))
        }

        // 2. Greedy syllable-by-syllable conversion
        val primary = greedyConvert(lower)

        // 3. Build candidate list (primary + alternatives from ambiguity map)
        val candidates = buildCandidates(lower, primary)

        return TransliterationResult(primary, candidates)
    }

    /**
     * Incremental transliteration: called on every keystroke in Singlish mode.
     * Returns partial conversion for display in the composing span.
     */
    fun transliteratePartial(input: String): String {
        if (input.isBlank()) return ""
        return greedyConvert(input.lowercase())
    }

    /**
     * Get all candidates for a given Singlish input.
     * Combines: primary greedy result + word shortcuts + ambiguity variants.
     */
    fun getCandidates(input: String): List<String> {
        return transliterate(input).candidates
    }

    // ─── Core greedy conversion ───────────────────────────────────────────

    private fun greedyConvert(input: String): String {
        val result = StringBuilder()
        var pos = 0

        while (pos < input.length) {
            var matched = false

            // Try matching from longest rule down to shortest
            for ((key, value) in sortedRules) {
                if (input.startsWith(key, pos)) {
                    // Found a match
                    val nextPos = pos + key.length
                    val nextChar = if (nextPos < input.length) input[nextPos] else null

                    // Determine if this is a consonant pattern needing vowel sign attachment
                    val sinhalaOutput = applyVowelSignIfNeeded(value, nextChar, input, nextPos)

                    result.append(sinhalaOutput.first)
                    pos += key.length + sinhalaOutput.second  // advance past vowel too if consumed

                    matched = true
                    break
                }
            }

            if (!matched) {
                // No rule matched: pass through character as-is
                result.append(input[pos])
                pos++
            }
        }

        return result.toString()
    }

    /**
     * If `sinhalaChar` is a consonant (ends with hal kirima ්),
     * check if the next Singlish chars form a vowel pattern.
     * If so, replace ් with the appropriate vowel sign and consume those chars.
     *
     * Returns: Pair(corrected sinhala string, extra chars consumed from input)
     */
    private fun applyVowelSignIfNeeded(
        sinhalaOutput: String,
        nextChar: Char?,
        input: String,
        nextPos: Int
    ): Pair<String, Int> {
        // Only apply if output ends with hal kirima (bare consonant)
        if (!sinhalaOutput.endsWith(SinhalaConstants.SIGN_AL) || nextChar == null) {
            return sinhalaOutput to 0
        }

        // Look ahead for vowel patterns (longest first)
        val vowelPatterns = listOf(
            "aae" to SinhalaConstants.SIGN_AAE,
            "aa"  to SinhalaConstants.SIGN_AA,
            "ae"  to SinhalaConstants.SIGN_AE,
            "ii"  to SinhalaConstants.SIGN_II,
            "uu"  to SinhalaConstants.SIGN_UU,
            "ee"  to SinhalaConstants.SIGN_EE,
            "oo"  to SinhalaConstants.SIGN_OO,
            "ai"  to SinhalaConstants.SIGN_AI,
            "au"  to SinhalaConstants.SIGN_AU,
            "a"   to null,            // inherent vowel — just remove the halant
            "i"   to SinhalaConstants.SIGN_I,
            "u"   to SinhalaConstants.SIGN_U,
            "e"   to SinhalaConstants.SIGN_E,
            "o"   to SinhalaConstants.SIGN_O,
        )

        val remaining = input.substring(nextPos)
        for ((pattern, sign) in vowelPatterns) {
            if (remaining.startsWith(pattern)) {
                // Remove the trailing ් from sinhala output
                val consonantOnly = sinhalaOutput.dropLast(1)
                val withSign = if (sign != null) {
                    consonantOnly + sign
                } else {
                    consonantOnly  // inherent 'a' — no sign needed
                }
                return withSign to pattern.length
            }
        }

        return sinhalaOutput to 0
    }

    // ─── Candidate generation ─────────────────────────────────────────────

    private fun buildCandidates(input: String, primary: String): List<String> {
        val candidates = mutableListOf(primary)

        // Check ambiguity map for the first syllable
        for ((key, alternatives) in SinglishRules.ambiguityMap) {
            if (input.startsWith(key)) {
                val tail = input.removePrefix(key)
                for (alt in alternatives) {
                    if (alt == primary) continue
                    val candidate = alt + if (tail.isNotEmpty()) greedyConvert(tail) else ""
                    if (!candidates.contains(candidate)) {
                        candidates.add(candidate)
                    }
                }
                break
            }
        }

        // Also check word shortcuts that start with the same prefix
        SinglishRules.wordShortcuts.entries
            .filter { it.key.startsWith(input) || input.startsWith(it.key) }
            .take(3)
            .forEach { (_, v) -> if (!candidates.contains(v)) candidates.add(v) }

        return candidates.take(5)  // max 5 candidates in suggestion bar
    }

    data class TransliterationResult(
        val primary: String,
        val candidates: List<String>
    )
}
