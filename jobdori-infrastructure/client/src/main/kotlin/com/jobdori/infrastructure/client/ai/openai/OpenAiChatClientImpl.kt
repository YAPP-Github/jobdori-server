package com.jobdori.infrastructure.client.ai.openai

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiGenerationRequest
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.infrastructure.client.ai.openai.dto.OpenAiChatCompletionRequest
import com.jobdori.infrastructure.client.ai.openai.dto.OpenAiChatCompletionResponse
import org.springframework.stereotype.Component


/**
 * [AiChatClient](채팅 생성)의 OpenAI 구현체.
 * Chat Completions(`/chat/completions`)를 호출한다.
 * HTTP 호출·에러 매핑은 [OpenAiHttpClient]에 위임.
 */
@Component
class OpenAiChatClientImpl(
    private val http: OpenAiHttpClient,
) : AiChatClient {

    override fun generateText(request: AiGenerationRequest): String {
        val body = OpenAiChatCompletionRequest.of(
            request.model,
            request.systemPrompt,
            request.userPrompt,
            request.parameters,
        )

        return call(body).textOrEmpty()
    }

    override fun <T : Any> generateStructured(request: AiStructuredRequest<T>): T {
        val format = OpenAiChatCompletionRequest.ResponseFormat.jsonSchema(
            name = request.responseType.simpleName ?: "structured_output",
            schemaJson = request.jsonSchema,
        )
        val body = OpenAiChatCompletionRequest.of(
            request.model, request.systemPrompt, request.userPrompt, request.parameters, format,
        )
        return call(body).parseContentAs(request.responseType.java)
    }

    private fun call(body: OpenAiChatCompletionRequest): OpenAiChatCompletionResponse =
        http.post("/chat/completions", body, OpenAiChatCompletionResponse::class.java)
}
