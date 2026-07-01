package com.jobdori.infrastructure.client.ai.openai.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException

@JsonIgnoreProperties(ignoreUnknown = true)
data class OpenAiEmbeddingResponse(
    val data: List<Item>
) {
    /** 첫 임베딩 벡터. 없으면 AiException. */
    fun firstEmbedding(): FloatArray =
        data.firstOrNull()?.embedding?.toFloatArray()
            ?: throw AiException("임베딩 응답 없음", AiErrorCode.E500_AI_GENERATION_FAILED)

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Item(
        val embedding: List<Float>
    )
}
