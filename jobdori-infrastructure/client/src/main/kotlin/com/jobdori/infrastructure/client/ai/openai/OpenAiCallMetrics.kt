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
        log.info {
            "ai_call useCase=$useCase model=$model success=true latencyMs=${elapsedMs(startedNanos)}" +
                " retries=$retries" +
                " promptTokens=${response.usage?.promptTokens} completionTokens=${response.usage?.completionTokens}" +
                " costUsd=${costUsd(model, response.usage)}" +
                " finishReason=${response.choices.firstOrNull()?.finishReason}"
        }
    }

    fun logFailure(useCase: String, model: String, startedNanos: Long, retries: Int, error: Throwable) {
        log.warn {
            "ai_call useCase=$useCase model=$model success=false latencyMs=${elapsedMs(startedNanos)}" +
                " retries=$retries error=${error.javaClass.simpleName}"
        }
    }

    private fun elapsedMs(startNanos: Long): Long = (System.nanoTime() - startNanos) / 1_000_000

    private fun costUsd(model: String, usage: OpenAiChatCompletionResponse.Usage?): String? {
        val (inputPrice, outputPrice) = PRICE_PER_1M_TOKENS_USD[model] ?: return null
        if (usage == null) return null
        val cost = (usage.promptTokens * inputPrice + usage.completionTokens * outputPrice) / 1_000_000
        return "%.6f".format(cost)
    }
}
