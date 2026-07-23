package com.jobdori.core.domain.experience.error

import com.jobdori.common.error.BaseException

data class ExperienceLimitExceededException(
    override val message: String = "프로젝트별 경험은 최대 10개까지 저장할 수 있습니다",
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ExperienceErrorCode.E400_EXPERIENCE_LIMIT_EXCEEDED,
    cause = cause,
)
