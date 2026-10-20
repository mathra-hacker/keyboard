package com.mathra.keyboard.sinhala

/**
 * SuggestionEngine generates word suggestions for both English and Sinhala/Singlish modes.
 *
 * For Singlish mode: wraps SinglishTransliterator to provide Sinhala word candidates.
 * For English mode: provides common-word completion from a built-in frequency list.
 *
 * This engine is designed to be extended — you can plug in a larger word list,
 * user history, or server-side suggestions without changing the IME code.
 */
class SuggestionEngine {

    private val transliterator = SinglishTransliterator()

    /**
     * Get suggestions for a Singlish input word.
     * Returns up to 5 Sinhala candidate strings.
     */
    fun getSinglishCandidates(input: String): List<String> {
        if (input.isBlank()) return emptyList()
        return transliterator.getCandidates(input)
    }

    /**
     * Get English word completions for a partial English word.
     * Returns up to 5 completions from the built-in frequency list.
     */
    fun getEnglishCompletions(partial: String): List<String> {
        if (partial.length < 2) return emptyList()
        val lower = partial.lowercase()
        return englishFrequencyList
            .filter { it.startsWith(lower) && it != lower }
            .take(5)
    }

    /**
     * Get Sinhala word completions for a partial Sinhala word (typed directly in Sinhala mode).
     * Returns up to 3 completions from the Sinhala word list.
     */
    fun getSinhalaCompletions(partial: String): List<String> {
        if (partial.isBlank()) return emptyList()
        return sinhalaCommonWords
            .filter { it.startsWith(partial) && it != partial }
            .take(3)
    }

    // ─── Built-in word lists ─────────────────────────────────────────────

    /**
     * High-frequency English words for basic completion.
     * In production: replace with a larger trie-based dictionary.
     */
    private val englishFrequencyList = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "it",
        "for", "not", "on", "with", "he", "as", "you", "do", "at", "this",
        "but", "his", "by", "from", "they", "we", "say", "her", "she", "or",
        "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know",
        "take", "people", "into", "year", "your", "good", "some", "could",
        "them", "see", "other", "than", "then", "now", "look", "only", "come",
        "its", "over", "think", "also", "back", "after", "use", "two", "how",
        "our", "work", "first", "well", "way", "even", "new", "want", "because",
        "any", "these", "give", "day", "most", "us",
        // Common Sri Lanka / context words
        "sri", "lanka", "sinhala", "colombo", "kandy", "galle",
        "please", "thank", "thanks", "hello", "sorry", "okay", "yes", "no",
        "home", "family", "phone", "call", "message", "school", "work", "food",
        "water", "time", "today", "tomorrow", "morning", "night", "evening",
        "happy", "love", "nice", "great", "fine", "good", "bad", "sad",
    )

    /**
     * High-frequency Sinhala words for direct Sinhala mode completion.
     */
    private val sinhalaCommonWords = listOf(
        "අම්මා", "තාත්තා", "මල්ලී", "අක්කා", "අයියා",
        "මම", "ඔය", "ඔයා", "ඔයාට", "අපි", "අපේ",
        "ඔව්", "නෑ", "හරි", "හොඳ", "හොඳයි",
        "කොහොමද", "කොහේද", "මොකද", "නේද",
        "ආයුබෝවන්", "ස්තූතියි", "කරුණාකරන්න",
        "සිංහල", "ශ්‍රී ලංකා", "ලංකාව",
        "ගම", "නගරය", "ගෙදර", "පාසල",
        "වෙලාව", "දවස", "රාත්‍රිය", "උදෑසන",
        "ආදරය", "සතුට", "දුක",
        "ජලය", "කෑම", "බත", "කිරි",
        "ලස්සන", "හොඳ", "නරක",
        "දේශය", "රට", "ජාතිය",
    )
}
