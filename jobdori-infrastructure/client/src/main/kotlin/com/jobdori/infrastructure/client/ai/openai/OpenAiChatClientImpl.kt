package com.jobdori.infrastructure.client.ai.openai

import com.jobdori.common.error.ErrorCode
import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiGenerationRequest
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.infrastructure.client.ai.openai.dto.OpenAiChatCompletionRequest
import com.jobdori.infrastructure.client.ai.openai.dto.OpenAiChatCompletionResponse
import datadog.trace.api.llmobs.LLMObs
import datadog.trace.api.llmobs.LLMObsSpan
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

        // 프롬프트 본문을 LLM Obs로 보내되 명백한 식별자는 지운다.
        // 이름/회사명 같은 문맥형 PII는 걸러지지 않으므로 Datadog 접근 권한 자체를 최소로 유지해야 한다.
        // 주민번호를 먼저 치환한다. 전화번호 패턴과 겹치는 입력에서 더 민감한 쪽을 놓치지 않기 위함.
        private val PII_PATTERNS = listOf(
            Regex("""\b\d{6}[-\s]?[1-4]\d{6}\b""") to "[rrn]",
            Regex("""[\w.+-]+@[\w-]+\.[\w.-]+""") to "[email]",
            Regex("""\b01[016-9][-.\s]?\d{3,4}[-.\s]?\d{4}\b""") to "[phone]",
        )

        private fun String.maskPii(): String =
            PII_PATTERNS.fold(this) { text, (pattern, replacement) -> text.replace(pattern, replacement) }
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
        // 계측 실패가 AI 호출을 깨뜨리면 안 된다. DD_TRACE_ENABLED=false면 startLLMSpan이 NPE를 던진다.
        val llmSpan = runCatching { LLMObs.startLLMSpan(useCase, body.model, "openai", null, null) }.getOrNull()
        try {
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
                .onSuccess { res ->
                    OpenAiCallMetrics.logSuccess(useCase, body.model, started, retries, res)
                    runCatching { llmSpan?.let { annotateLlmSpan(it, body, res) } }
                }
                .onFailure { e ->
                    OpenAiCallMetrics.logFailure(useCase, body.model, started, retries, e)
                    runCatching { llmSpan?.addThrowable(e) }
                }
                .getOrThrow()
        } finally {
            runCatching { llmSpan?.finish() }
        }
    }

    private fun annotateLlmSpan(
        span: LLMObsSpan,
        body: OpenAiChatCompletionRequest,
        res: OpenAiChatCompletionResponse,
    ) {
        span.annotateIO(
            body.messages.map { LLMObs.LLMMessage.from(it.role, it.content.maskPii()) },
            res.choices.map { LLMObs.LLMMessage.from(it.message.role, it.message.content.maskPii()) },
        )
        res.usage?.let {
            span.setMetrics(
                mapOf(
                    "input_tokens" to it.promptTokens,
                    "output_tokens" to it.completionTokens,
                    "total_tokens" to it.totalTokens,
                ),
            )
        }
    }
}
