package com.mathra.keyboard.sinhala

/**
 * All Sinhala Unicode code points used by the keyboard engine.
 * Unicode block: U+0D80–U+0DFF (Sinhala)
 */
object SinhalaConstants {

    // ─── Independent Vowels ───────────────────────────────────────────────
    const val VOWEL_A    = '\u0D85'  // අ
    const val VOWEL_AA   = '\u0D86'  // ආ
    const val VOWEL_AE   = '\u0D87'  // ඇ
    const val VOWEL_AAE  = '\u0D88'  // ඈ
    const val VOWEL_I    = '\u0D89'  // ඉ
    const val VOWEL_II   = '\u0D8A'  // ඊ
    const val VOWEL_U    = '\u0D8B'  // උ
    const val VOWEL_UU   = '\u0D8C'  // ඌ
    const val VOWEL_RU   = '\u0D8D'  // ඍ
    const val VOWEL_RUU  = '\u0D8E'  // ඎ
    const val VOWEL_LU   = '\u0D8F'  // ඏ
    const val VOWEL_LUU  = '\u0D90'  // ඐ
    const val VOWEL_E    = '\u0D91'  // එ
    const val VOWEL_EE   = '\u0D92'  // ඒ
    const val VOWEL_AI   = '\u0D93'  // ඓ
    const val VOWEL_O    = '\u0D94'  // ඔ
    const val VOWEL_OO   = '\u0D95'  // ඕ
    const val VOWEL_AU   = '\u0D96'  // ඖ

    // ─── Consonants ──────────────────────────────────────────────────────
    const val CONS_KA    = '\u0D9A'  // ක
    const val CONS_KHA   = '\u0D9B'  // ඛ
    const val CONS_GA    = '\u0D9C'  // ග
    const val CONS_GHA   = '\u0D9D'  // ඝ
    const val CONS_NGA   = '\u0D9E'  // ඞ
    const val CONS_NGKA  = '\u0D9F'  // ඟ
    const val CONS_CA    = '\u0DA0'  // ච
    const val CONS_CHA   = '\u0DA1'  // ඡ
    const val CONS_JA    = '\u0DA2'  // ජ
    const val CONS_JHA   = '\u0DA3'  // ඣ
    const val CONS_NYA   = '\u0DA4'  // ඤ
    const val CONS_JNYA  = '\u0DA5'  // ඥ
    const val CONS_NJA   = '\u0DA6'  // ඦ
    const val CONS_TTA   = '\u0DA7'  // ට
    const val CONS_TTHA  = '\u0DA8'  // ඨ
    const val CONS_DDA   = '\u0DA9'  // ඩ
    const val CONS_DDHA  = '\u0DAA'  // ඪ
    const val CONS_NNA   = '\u0DAB'  // ණ
    const val CONS_NNDDA = '\u0DAC'  // ඬ
    const val CONS_TA    = '\u0DAD'  // ත
    const val CONS_THA   = '\u0DAE'  // ථ
    const val CONS_DA    = '\u0DAF'  // ද
    const val CONS_DHA   = '\u0DB0'  // ධ
    const val CONS_NA    = '\u0DB1'  // න
    const val CONS_NDa   = '\u0DB3'  // ඳ
    const val CONS_PA    = '\u0DB4'  // ප
    const val CONS_PHA   = '\u0DB5'  // ඵ
    const val CONS_BA    = '\u0DB6'  // බ
    const val CONS_BHA   = '\u0DB7'  // භ
    const val CONS_MA    = '\u0DB8'  // ම
    const val CONS_MBA   = '\u0DB9'  // ඹ
    const val CONS_YA    = '\u0DBA'  // ය
    const val CONS_RA    = '\u0DBB'  // ර
    const val CONS_LA    = '\u0DBD'  // ල
    const val CONS_VA    = '\u0DC0'  // ව
    const val CONS_SHA   = '\u0DC1'  // ශ
    const val CONS_SSA   = '\u0DC2'  // ෂ
    const val CONS_SA    = '\u0DC3'  // ස
    const val CONS_HA    = '\u0DC4'  // හ
    const val CONS_LLA   = '\u0DC5'  // ළ
    const val CONS_FA    = '\u0DC6'  // ෆ

    // ─── Dependent Vowel Signs (combine with consonant) ───────────────────
    const val SIGN_AA    = '\u0DCF'  // ා
    const val SIGN_AE    = '\u0DD0'  // ැ
    const val SIGN_AAE   = '\u0DD1'  // ෑ
    const val SIGN_I     = '\u0DD2'  // ි
    const val SIGN_II    = '\u0DD3'  // ී
    const val SIGN_U     = '\u0DD4'  // ු
    const val SIGN_UU    = '\u0DD6'  // ූ
    const val SIGN_R     = '\u0DD8'  // ෘ
    const val SIGN_E     = '\u0DD9'  // ෙ
    const val SIGN_EE    = '\u0DDA'  // ේ
    const val SIGN_AI    = '\u0DDB'  // ෛ
    const val SIGN_O     = '\u0DDC'  // ො
    const val SIGN_OO    = '\u0DDD'  // ෝ
    const val SIGN_AU    = '\u0DDE'  // ෞ

    // ─── Special Marks ───────────────────────────────────────────────────
    const val SIGN_AL    = '\u0DCA'  // ් (hal kirima / halant — kills inherent vowel)
    const val ANUSVARA   = '\u0D82'  // ං (anusvara — "ng" nasal)
    const val VISARGA    = '\u0D83'  // ඃ (visarga)
    const val SIGN_RR    = '\u0DDF'  // ෟ
    const val SIGN_ALUDA = '\u0DF2'  // ෲ

    // ─── Digits ──────────────────────────────────────────────────────────
    val SINHALA_DIGITS = charArrayOf(
        '\u0DE6', '\u0DE7', '\u0DE8', '\u0DE9', '\u0DEA',
        '\u0DEB', '\u0DEC', '\u0DED', '\u0DEE', '\u0DEF'
    ) // ෦ ෧ ෨ ෩ ෪ ෫ ෬ ෭ ෮ ෯

    // ─── Zero Width Joiner / Non-Joiner ──────────────────────────────────
    const val ZWJ  = '\u200D'  // Zero Width Joiner — used for Rakaaraansaya, Yansaya
    const val ZWNJ = '\u200C'  // Zero Width Non-Joiner
}
