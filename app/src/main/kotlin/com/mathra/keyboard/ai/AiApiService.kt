package com.mathra.keyboard.ai

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit interface for the AI writing assistant.
 *
 * The endpoint URL is configurable at runtime (set by user in AI settings).
 * The API key is stored in EncryptedSharedPreferences and passed as a header.
 *
 * Default implementation targets the OpenAI-compatible chat completion API format.
 * You can point this at any backend that speaks the same JSON schema.
 *
 * ⚠️ No API keys are hardcoded anywhere in this file or any other source file.
 */
interface AiApiService {

    @POST
    suspend fun complete(
        @Url url: String,
        @Header("Authorization") bearerToken: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: AiRequest
    ): Response<AiResponse>
}

// ─── Request / Response DTOs ──────────────────────────────────────────────────

data class AiRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<AiMessage>,
    val max_tokens: Int = 500,
    val temperature: Double = 0.7
)

data class AiMessage(
    val role: String,   // "system" | "user" | "assistant"
    val content: String
)

data class AiResponse(
    val choices: List<AiChoice>?,
    val error: AiError?
)

data class AiChoice(
    val message: AiMessage?,
    val finish_reason: String?
)

data class AiError(
    val message: String?,
    val type: String?
)
