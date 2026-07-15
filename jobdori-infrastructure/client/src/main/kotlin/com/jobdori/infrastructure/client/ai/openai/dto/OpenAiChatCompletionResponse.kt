package com.jobdori.infrastructure.client.ai.openai.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.jobdori.common.json.JsonUtils
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException

/**
 * OpenAI Chat Completions API 응답 DTO (필요한 최소 필드만).
 * 응답에 우리가 안 쓰는 필드가 많으므로 알 수 없는 필드는 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiChatCompletionResponse(
    val choices: List<Choice>,
    val usage: Usage? = null,
) {
    fun textOrEmpty(): String = choices.firstOrNull()?.message?.content.orEmpty()

    fun <T : Any> parseContentAs(type: Class<T>): T {
        val content = choices.firstOrNull()?.message?.content
            ?: throw AiException("빈 응답", AiErrorCode.E500_AI_GENERATION_FAILED)
        return runCatching { JsonUtils.toObject(content, type) }.getOrNull()
            ?: throw AiException("구조화 응답 역직렬화 실패", AiErrorCode.E500_AI_GENERATION_FAILED)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Choice(
        val message: Message,
        @JsonProperty("finish_reason") val finishReason: String? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Message(
        val role: String,
        val content: String,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Usage(
        @JsonProperty("prompt_tokens") val promptTokens: Int,
        @JsonProperty("completion_tokens") val completionTokens: Int,
        @JsonProperty("total_tokens") val totalTokens: Int,
    )
}
