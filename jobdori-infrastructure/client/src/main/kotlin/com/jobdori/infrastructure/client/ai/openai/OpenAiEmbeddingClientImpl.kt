package com.jobdori.infrastructure.client.ai.openai

import com.jobdori.core.application.ai.client.AiEmbeddingClient
import com.jobdori.infrastructure.client.ai.openai.dto.OpenAiEmbeddingRequest
import com.jobdori.infrastructure.client.ai.openai.dto.OpenAiEmbeddingResponse
import org.springframework.stereotype.Component

/**
 * [AiEmbeddingClient]의 OpenAI 구현체.
 * Embeddings(`/embeddings`, text-embedding-3-small)를 호출한다.
 * HTTP 호출·에러 매핑은 [OpenAiHttpClient]에 위임.
 */
@Component
class OpenAiEmbeddingClientImpl(
    private val http: OpenAiHttpClient,
) : AiEmbeddingClient {

    companion object {
        // 매칭용 단일 모델.
        private const val EMBEDDING_MODEL = "text-embedding-3-small"
    }

    override fun embed(text: String): FloatArray =
        http.post("/embeddings",
            OpenAiEmbeddingRequest(
                EMBEDDING_MODEL,
                text
            ),
            OpenAiEmbeddingResponse::class.java
        ).firstEmbedding()
}
