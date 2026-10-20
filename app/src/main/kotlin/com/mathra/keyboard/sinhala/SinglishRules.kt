package com.mathra.keyboard.sinhala

/**
 * Complete Singlish-to-Sinhala phonetic transliteration rule table.
 *
 * Rules are stored as a sorted list of (romanized pattern → Sinhala output) pairs.
 * Longer patterns take priority over shorter ones during matching (greedy left-to-right).
 *
 * The ambiguityMap stores patterns that can map to multiple Sinhala outputs,
 * presented to the user as candidate suggestions.
 *
 * Design: this file is the single source of truth for phonetics.
 * Add or correct entries here without touching the engine code.
 */
object SinglishRules {

    /**
     * Primary rule map: romanized string → single best Sinhala string.
     * Keys are sorted by descending length for greedy matching in the engine.
     */
    val rules: Map<String, String> = buildMap {
        // ─── Vowels (standalone) ──────────────────────────────────────────
        put("aa",  "\u0D86")  // ආ
        put("ae",  "\u0D87")  // ඇ
        put("aae", "\u0D88")  // ඈ
        put("ii",  "\u0D8A")  // ඊ
        put("uu",  "\u0D8C")  // ඌ
        put("ee",  "\u0D92")  // ඒ
        put("oo",  "\u0D95")  // ඕ
        put("ai",  "\u0D93")  // ඓ
        put("au",  "\u0D96")  // ඖ
        put("a",   "\u0D85")  // අ
        put("A",   "\u0D86")  // ආ
        put("i",   "\u0D89")  // ඉ
        put("I",   "\u0D8A")  // ඊ
        put("u",   "\u0D8B")  // උ
        put("U",   "\u0D8C")  // ඌ
        put("e",   "\u0D91")  // එ
        put("E",   "\u0D92")  // ඒ
        put("o",   "\u0D94")  // ඔ
        put("O",   "\u0D95")  // ඕ

        // ─── Consonants (4-char patterns first) ───────────────────────────
        put("ndda", "\u0DAC")  // ඬ
        put("nndda","\u0DAC")

        // ─── Consonants (3-char patterns) ─────────────────────────────────
        put("kha",  "\u0D9B")  // ඛ
        put("gha",  "\u0D9D")  // ඝ
        put("nga",  "\u0D9E")  // ඞ
        put("cha",  "\u0DA1")  // ඡ
        put("jha",  "\u0DA3")  // ඣ
        put("nya",  "\u0DA4")  // ඤ
        put("tta",  "\u0DA7")  // ට  — alternate
        put("ttha", "\u0DA8")  // ඨ
        put("dda",  "\u0DA9")  // ඩ
        put("ddha", "\u0DAA")  // ඪ
        put("nna",  "\u0DAB")  // ණ
        put("tha",  "\u0DAE")  // ථ
        put("dha",  "\u0DB0")  // ධ
        put("nda",  "\u0DB3")  // ඳ
        put("pha",  "\u0DB5")  // ඵ
        put("bha",  "\u0DB7")  // භ
        put("mba",  "\u0DB9")  // ඹ
        put("sha",  "\u0DC1")  // ශ
        put("ssa",  "\u0DC2")  // ෂ
        put("lla",  "\u0DC5")  // ළ
        put("shri", "\u0DC1\u0DCA\u200D\u0DBB\u0DD2")  // ශ්‍රි

        // ─── Consonants (2-char patterns) ─────────────────────────────────
        put("ka",  "\u0D9A")  // ක
        put("ga",  "\u0D9C")  // ග
        put("ca",  "\u0DA0")  // ච
        put("ja",  "\u0DA2")  // ජ
        put("ta",  "\u0DAD")  // ත  (dental)
        put("da",  "\u0DAF")  // ද
        put("na",  "\u0DB1")  // න
        put("pa",  "\u0DB4")  // ප
        put("ba",  "\u0DB6")  // බ
        put("ma",  "\u0DB8")  // ම
        put("ya",  "\u0DBA")  // ය
        put("ra",  "\u0DBB")  // ර
        put("la",  "\u0DBD")  // ල
        put("va",  "\u0DC0")  // ව
        put("wa",  "\u0DC0")  // ව (alternate)
        put("sa",  "\u0DC3")  // ස
        put("ha",  "\u0DC4")  // හ
        put("fa",  "\u0DC6")  // ෆ

        // ─── Single consonant keys ────────────────────────────────────────
        put("k",  "\u0D9A\u0DCA")  // ක්
        put("g",  "\u0D9C\u0DCA")  // ග්
        put("c",  "\u0DA0\u0DCA")  // ච්
        put("j",  "\u0DA2\u0DCA")  // ජ්
        put("t",  "\u0DAD\u0DCA")  // ත්
        put("d",  "\u0DAF\u0DCA")  // ද්
        put("n",  "\u0DB1\u0DCA")  // න්
        put("p",  "\u0DB4\u0DCA")  // ප්
        put("b",  "\u0DB6\u0DCA")  // බ්
        put("m",  "\u0DB8\u0DCA")  // ම්
        put("y",  "\u0DBA\u0DCA")  // ය්
        put("r",  "\u0DBB\u0DCA")  // ර්
        put("l",  "\u0DBD\u0DCA")  // ල්
        put("v",  "\u0DC0\u0DCA")  // ව්
        put("w",  "\u0DC0\u0DCA")  // ව් (alternate)
        put("s",  "\u0DC3\u0DCA")  // ස්
        put("h",  "\u0DC4\u0DCA")  // හ්
        put("f",  "\u0DC6\u0DCA")  // ෆ්

        // ─── Nasal / Anusvara ─────────────────────────────────────────────
        put("ng",  "\u0D82")  // ං
        put("N",   "\u0DAB")  // ණ
        put("M",   "\u0DB8")  // ම

        // ─── Common syllable patterns (high-frequency words) ──────────────
        put("nka",  "\u0D9A\u0DCA\u0D9A")   // ංකා? handled by composer
        put("mma",  "\u0DB8\u0DCA\u0DB8")   // ම්ම
        put("nna",  "\u0DB1\u0DCA\u0DB1")   // න්න  (overrides single nna above)
        put("lla",  "\u0DBD\u0DCA\u0DBD")   // ල්ල
        put("ssa",  "\u0DC3\u0DCA\u0DC3")   // ස්ස

        // ─── Vowel signs (post-consonant) — handled dynamically by composer
        // These are not in this map; composer adds them based on next char
    }

    /**
     * Words with ambiguous transliterations.
     * The engine offers these as candidate suggestions.
     * key = romanized word, value = list of Sinhala options (best first)
     */
    val ambiguityMap: Map<String, List<String>> = mapOf(
        "ka"  to listOf("ක", "කා"),
        "ko"  to listOf("කො", "කෝ"),
        "n"   to listOf("න්", "ං", "ණ්"),
        "l"   to listOf("ල්", "ළ්"),
        "na"  to listOf("නා", "ණා"),
        "la"  to listOf("ලා", "ළා"),
        "sha" to listOf("ශ", "ෂ"),
        "ta"  to listOf("ත", "ට"),
        "da"  to listOf("ද", "ඩ"),
        "t"   to listOf("ත්", "ට්"),
        "d"   to listOf("ද්", "ඩ්"),
        "nna" to listOf("ණ", "න්න"),
        "dda" to listOf("ඩ", "ද්ද"),
        "tta" to listOf("ට", "ත්ත"),
    )

    /**
     * Common full-word shortcuts for high-frequency Sinhala words.
     * These are checked before syllable-by-syllable transliteration.
     */
    val wordShortcuts: Map<String, String> = mapOf(
        "amma"      to "අම්මා",
        "thaththa"  to "තාත්තා",
        "malli"     to "මල්ලී",
        "akka"      to "අක්කා",
        "ayya"      to "අයියා",
        "nanda"     to "නැන්දා",
        "mama"      to "මම",
        "oya"       to "ඔය",
        "oyaa"      to "ඔයා",
        "oyata"     to "ඔයාට",
        "mage"      to "මගේ",
        "ape"       to "අපේ",
        "api"       to "අපි",
        "eka"       to "එක",
        "ekata"     to "එකට",
        "kohe"      to "කොහේ",
        "koheda"    to "කොහේද",
        "kohomada"  to "කොහොමද",
        "kohoma"    to "කොහොම",
        "mokakda"   to "මොකක්ද",
        "mokada"    to "මොකද",
        "neda"      to "නේද",
        "nehe"      to "නෑ",
        "naa"       to "නෑ",
        "ow"        to "ඔව්",
        "awu"       to "ඔව්",
        "hari"      to "හරි",
        "hondai"    to "හොඳයි",
        "honda"     to "හොඳ",
        "honda"     to "හොඳ",
        "lassana"   to "ලස්සන",
        "sthuthi"   to "ස්තූති",
        "bohoma"    to "බොහොම",
        "isthuthy"  to "ඉස්තූති",
        "suba"      to "සුභ",
        "sthree"    to "ස්ත්‍රී",
        "gama"      to "ගම",
        "rata"      to "රට",
        "desa"      to "දේශ",
        "deshaya"   to "දේශය",
        "puwath"    to "පුවත්",
        "sinhala"   to "සිංහල",
        "sinhalaya" to "සිංහලයා",
        "lankawe"   to "ලංකාවේ",
        "srilanka"  to "ශ්‍රී ලංකා",
        "ayubowan"  to "ආයුබෝවන්",
        "isthuthi"  to "ඉස්තූතියි",
        "karunakaranna" to "කරුණාකරන්න",
        "karunakara" to "කරුණාකර",
    )
}
