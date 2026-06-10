package com.jobdori.infrastructure.client.ai.dto

/**
 * OpenAI Chat Completions API 요청 DTO (필요한 최소 필드만).
 */
data class ChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
) {
    data class Message(
        val role: String,
        val content: String,
    )
}
