package com.mathra.keyboard.ai

import android.content.Context
import com.mathra.keyboard.util.PrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * AiAssistant: repository layer for AI writing assistance.
 *
 * Privacy guarantees:
 *  - Text is only sent when the user EXPLICITLY calls one of the assist* functions.
 *  - No background keylogging.
 *  - Password fields must be filtered before calling — checked in MathraIME.
 *  - API key is read from EncryptedSharedPreferences at call time, never cached in memory longer than needed.
 *  - Disabled entirely if user has not enabled AI in settings.
 */
class AiAssistant(private val context: Context) {

    // ─── Retrofit setup (lazy — only built when AI is actually used) ──────

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    // We use a dummy base URL because @Url overrides it per-call
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")  // default; overridden by @Url param
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val service by lazy { retrofit.create(AiApiService::class.java) }

    // ─── Public API ───────────────────────────────────────────────────────

    sealed class AiResult {
        data class Success(val text: String) : AiResult()
        data class Error(val message: String) : AiResult()
        object Disabled : AiResult()
        object NoKey : AiResult()
    }

    /**
     * Improve the writing style of the provided text.
     */
    suspend fun improveWriting(text: String): AiResult =
        callAI(
            systemPrompt = "You are a writing assistant. Improve the style, clarity and grammar of the text. Return only the improved text, nothing else.",
            userText = text
        )

    /**
     * Fix grammar in the provided text.
     */
    suspend fun fixGrammar(text: String): AiResult =
        callAI(
            systemPrompt = "Fix any grammar errors in the following text. Return only the corrected text, nothing else.",
            userText = text
        )

    /**
     * Translate text to Sinhala.
     */
    suspend fun translateToSinhala(text: String): AiResult =
        callAI(
            systemPrompt = "Translate the following text to Sinhala. Return only the translation, nothing else.",
            userText = text
        )

    /**
     * Translate text to English.
     */
    suspend fun translateToEnglish(text: String): AiResult =
        callAI(
            systemPrompt = "Translate the following text to English. Return only the translation, nothing else.",
            userText = text
        )

    // ─── Internal ─────────────────────────────────────────────────────────

    private suspend fun callAI(systemPrompt: String, userText: String): AiResult {
        if (userText.isBlank()) return AiResult.Error("No text provided.")

        if (!PrefsManager.getBoolean(context, PrefsManager.KEY_AI_ENABLED, false)) {
            return AiResult.Disabled
        }

        val apiKey = PrefsManager.getSecureString(context, PrefsManager.KEY_AI_API_KEY)
        if (apiKey.isBlank()) return AiResult.NoKey

        val endpoint = PrefsManager.getString(
            context,
            PrefsManager.KEY_AI_ENDPOINT,
            "https://api.openai.com/v1/chat/completions"
        )
        if (endpoint.isBlank() || !endpoint.startsWith("https://")) {
            return AiResult.Error("Invalid API endpoint. Must be an HTTPS URL.")
        }

        return withContext(Dispatchers.IO) {
            try {
                val request = AiRequest(
                    messages = listOf(
                        AiMessage("system", systemPrompt),
                        AiMessage("user", userText)
                    )
                )
                val response = service.complete(
                    url = endpoint,
                    bearerToken = "Bearer $apiKey",
                    request = request
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    val result = body?.choices?.firstOrNull()?.message?.content
                    if (result != null) AiResult.Success(result.trim())
                    else AiResult.Error("Empty response from AI.")
                } else {
                    val errorBody = response.body()?.error?.message ?: "HTTP ${response.code()}"
                    AiResult.Error("AI error: $errorBody")
                }
            } catch (e: java.net.UnknownHostException) {
                AiResult.Error("No internet connection.")
            } catch (e: java.util.concurrent.TimeoutException) {
                AiResult.Error("Request timed out. Try again.")
            } catch (e: Exception) {
                AiResult.Error("Unexpected error: ${e.message}")
            }
        }
    }
}
