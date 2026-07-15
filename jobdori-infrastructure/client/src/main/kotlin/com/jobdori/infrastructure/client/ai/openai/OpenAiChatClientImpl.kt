package com.jobdori.infrastructure.client.ai.openai

import com.jobdori.common.error.ErrorCode
import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiGenerationRequest
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
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

    companion object {
        // 429(한도)/503(프로바이더 장애)만 재시도. 504(타임아웃)는 읽기 타임아웃이 60초라
        // 재시도하면 사용자 요청이 분 단위로 매달리므로 즉시 실패시킨다.
        private val RETRYABLE_ERRORS: Set<ErrorCode> =
            setOf(AiErrorCode.E429_AI_RATE_LIMITED, AiErrorCode.E503_AI_UNAVAILABLE)
        private const val MAX_RETRIES = 2
        private const val RETRY_BACKOFF_MS = 500L
    }

    override fun generateText(request: AiGenerationRequest): String {
        val body = OpenAiChatCompletionRequest.of(
            request.model,
            request.systemPrompt,
            request.userPrompt,
            request.parameters,
        )

        return call(request.useCase, body).textOrEmpty()
    }

    override fun <T : Any> generateStructured(request: AiStructuredRequest<T>): T {
        val format = OpenAiChatCompletionRequest.ResponseFormat.jsonSchema(
            name = request.responseType.simpleName ?: "structured_output",
            schemaJson = request.jsonSchema,
        )
        val body = OpenAiChatCompletionRequest.of(
            request.model, request.systemPrompt, request.userPrompt, request.parameters, format,
        )
        return call(request.useCase, body).parseContentAs(request.responseType.java)
    }

    private fun call(useCase: String, body: OpenAiChatCompletionRequest): OpenAiChatCompletionResponse {
        val started = System.nanoTime()
        var retries = 0
        return runCatching {
            var result: OpenAiChatCompletionResponse? = null
            while (result == null) {
                result = try {
                    http.post("/chat/completions", body, OpenAiChatCompletionResponse::class.java)
                } catch (e: AiException) {
                    if (retries >= MAX_RETRIES || e.errorCode !in RETRYABLE_ERRORS) throw e
                    retries++
                    Thread.sleep(RETRY_BACKOFF_MS * retries)
                    null
                }
            }
            result
        }
            .onSuccess { res -> OpenAiCallMetrics.logSuccess(useCase, body.model, started, retries, res) }
            .onFailure { e -> OpenAiCallMetrics.logFailure(useCase, body.model, started, retries, e) }
            .getOrThrow()
    }
}
