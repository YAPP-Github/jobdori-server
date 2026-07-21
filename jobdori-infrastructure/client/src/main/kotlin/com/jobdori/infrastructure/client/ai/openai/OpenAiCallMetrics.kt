package com.jobdori.infrastructure.client.ai.openai

import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.infrastructure.client.ai.openai.dto.OpenAiChatCompletionResponse

/**
 * AI 호출당 사용량 로그(ai_call ...) 한 줄을 남긴다. 이 로그가 AI 비용/성능 지표의 원천이므로
 * key=value 형식과 키 이름은 로그 쿼리 호환성을 위해 함부로 바꾸지 않는다.
 */
object OpenAiCallMetrics {

    // 1M 토큰당 USD 단가 (input, output). DB에서 모델을 교체/추가하면 여기도 갱신해야 비용이 찍힌다.
    // 모르는 모델은 costUsd=null로 남으므로 로그에서 누락을 알 수 있다.
    private val PRICE_PER_1M_TOKENS_USD = mapOf(
        "gpt-4o-mini" to (0.15 to 0.60),
        "gpt-4o" to (2.50 to 10.00),
    )

    fun logSuccess(
        useCase: String,
        model: String,
        startedNanos: Long,
        retries: Int,
        response: OpenAiChatCompletionResponse,
    ) {
        val fields = linkedMapOf<String, Any?>(
            "useCase" to useCase,
            "model" to model,
            "success" to true,
            "latencyMs" to elapsedMs(startedNanos),
            "retries" to retries,
            "promptTokens" to response.usage?.promptTokens,
            "completionTokens" to response.usage?.completionTokens,
            "costUsd" to costUsd(model, response.usage),
            "finishReason" to response.choices.firstOrNull()?.finishReason,
        )
        log.atInfo {
            message = render(fields)
            payload = fields
        }
    }

    fun logFailure(useCase: String, model: String, startedNanos: Long, retries: Int, error: Throwable) {
        val fields = linkedMapOf<String, Any?>(
            "useCase" to useCase,
            "model" to model,
            "success" to false,
            "latencyMs" to elapsedMs(startedNanos),
            "retries" to retries,
            "error.kind" to error.javaClass.simpleName,
        )
        log.atWarn {
            message = render(fields)
            payload = fields
        }
    }

    // payload는 JSON 로깅 시 최상위 필드(facet)로, message는 기존 전문 검색 쿼리 호환을 위해 둘 다 남긴다.
    private fun render(fields: Map<String, Any?>): String =
        fields.entries.joinToString(separator = " ", prefix = "ai_call ") { "${it.key}=${it.value}" }

    private fun elapsedMs(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000

    private fun costUsd(model: String, usage: OpenAiChatCompletionResponse.Usage?): String? {
        val (inputPrice, outputPrice) = PRICE_PER_1M_TOKENS_USD[model] ?: return null
        if (usage == null) return null
        val cost = (usage.promptTokens * inputPrice + usage.completionTokens * outputPrice) / 1_000_000
        return "%.6f".format(cost)
    }
}
