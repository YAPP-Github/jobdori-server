package com.jobdori.core.application.ai.client

import com.jobdori.core.application.ai.command.AiGenerationRequest
import com.jobdori.core.application.ai.command.AiStructuredRequest

/**
 * AI 채팅 생성 추상화(외부 연동 포트).
 *
 * core는 "AI에게 프롬프트를 주면 텍스트/구조화 출력을 받는다"는 계약만 알고,
 * 실제 어떤 외부 서비스(OpenAI 등)를 쓰는지는 infrastructure가 구현한다.
 * 임베딩은 엔드포인트·관심사가 달라 [AiEmbeddingClient]로 분리한다(ISP).
 */
interface AiChatClient {
    /** 자유 텍스트 생성 (경험 문장 작성, 톤 변환 등) */
    fun generateText(request: AiGenerationRequest): String

    /** JSON 스키마를 강제한 구조화 출력 (STAR 추출, JD 분석 등) */
    fun <T : Any> generateStructured(request: AiStructuredRequest<T>): T
}
