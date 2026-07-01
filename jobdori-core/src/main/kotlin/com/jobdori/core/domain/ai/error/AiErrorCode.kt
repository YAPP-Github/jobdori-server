package com.jobdori.core.domain.ai.error

import com.jobdori.common.error.ErrorCode

enum class AiErrorCode(
    override val httpStatusCode: Int,
    override val code: String,
    override val message: String,
    override val description: String,
): ErrorCode {
    E429_AI_RATE_LIMITED(
        httpStatusCode = 429,
        code = "ai_rate_limited",
        message = "AI 요청이 많아 잠시 후 다시 시도해 주세요.",
        description = "AI 요청 한도 초과",
    ),
    E503_AI_UNAVAILABLE(
        httpStatusCode = 503,
        code = "ai_unavailable",
        message = "현재 AI 서비스를 이용할 수 없습니다. 잠시 후 다시 시도해 주세요.",
        description = "AI 서비스 접근 불가",
    ),
    E504_AI_TIMEOUT(
        httpStatusCode = 504,
        code = "ai_timeout",
        message = "AI 응답이 지연되고 있습니다. 잠시 후 다시 시도해 주세요.",
        description = "AI 응답 지연/시간 초과",
    ),
    E500_AI_GENERATION_FAILED(
        httpStatusCode = 500,
        code = "ai_generation_failed",
        message = "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
        description = "AI 응답 생성 실패",
    ),
}
