package com.jobdori.core.application.ai.client

/**
 * 텍스트 임베딩 추상화(외부 연동 포트).
 *
 * 채팅([AiChatClient])과 달리 별도 엔드포인트(`/embeddings`)
 * 모델(text-embedding-3-small)을 쓰고
 * system/user 프롬프트 개념이 없어 포트를 분리한다(ISP).
 * 매칭(JD-경험 유사도)에서만 사용한다.
 */
interface AiEmbeddingClient {
    /** 텍스트 임베딩 (매칭용) */
    fun embed(text: String): FloatArray
}
