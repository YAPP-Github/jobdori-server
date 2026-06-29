package com.jobdori.core.domain.ai.error

import com.jobdori.common.error.ErrorCode

enum class AiErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val description: String,
): ErrorCode {
    E429_AI_RATE_LIMITED(
        429,
        "ai_rate_limited",
        "AI 요청 한도 초과"
    ),
    E503_AI_UNAVAILABLE(
        503,
        "ai_unavailable",
        "AI 서비스 접근 불가"
    ),
    E504_AI_TIMEOUT(
        504,
        "ai_timeout",
        "AI 응답 지연/시간 초과"
    ),
    E500_AI_GENERATION_FAILED(
        500,
        "ai_generation_failed",
        "AI 응답 생성 실패"
    ),
}
