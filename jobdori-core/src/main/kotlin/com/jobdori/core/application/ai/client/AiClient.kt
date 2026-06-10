package com.jobdori.core.application.ai.client

/**
 * AI 텍스트 생성 추상화(외부 연동 포트).
 *
 * core는 "AI에게 프롬프트를 주면 텍스트를 받는다"는 계약만 알고,
 * 실제 어떤 외부 서비스(OpenAI 등)를 쓰는지는 infrastructure가 구현한다.
 */
interface AiClient {
    fun generateText(prompt: String): String
}
