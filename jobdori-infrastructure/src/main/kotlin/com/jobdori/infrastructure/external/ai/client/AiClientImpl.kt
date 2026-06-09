package com.jobdori.infrastructure.external.ai.client

import com.jobdori.core.application.ai.client.AiClient
import com.jobdori.infrastructure.external.ai.dto.ChatCompletionRequest
import com.jobdori.infrastructure.external.ai.dto.ChatCompletionResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * [AiClient]의 OpenAI 구현체.
 * core 인터페이스를 구현하며, 실제 OpenAI Chat Completions API를 호출한다.
 */
@Component
class AiClientImpl(
    @Value("\${openai.api-key}") private val apiKey: String,
) : AiClient {

    private val restClient = RestClient.builder()
        .baseUrl("https://api.openai.com/v1")
        .build()

    override fun generateText(prompt: String): String {
        val request = ChatCompletionRequest(
            model = "gpt-4o-mini",
            messages = listOf(
                ChatCompletionRequest.Message(role = "user", content = prompt),
            ),
        )

        val response = restClient.post()
            .uri("/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .body(request)
            .retrieve()
            .body(ChatCompletionResponse::class.java)

        // 응답에서 첫 번째 답변 텍스트만 추출 (변환이 단순해 별도 mapper는 두지 않음)
        return response?.choices?.firstOrNull()?.message?.content.orEmpty()
    }
}
