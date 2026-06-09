package com.jobdori.infrastructure.external.ai.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * OpenAI Chat Completions API 응답 DTO (필요한 최소 필드만).
 * 응답에 우리가 안 쓰는 필드가 많으므로 알 수 없는 필드는 무시한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ChatCompletionResponse(
    val choices: List<Choice>,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Choice(
        val message: Message,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Message(
        val role: String,
        val content: String,
    )
}
