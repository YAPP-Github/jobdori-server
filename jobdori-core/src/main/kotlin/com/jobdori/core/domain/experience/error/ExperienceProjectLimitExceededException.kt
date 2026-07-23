package com.jobdori.core.domain.experience.error

import com.jobdori.common.error.BaseException

data class ExperienceProjectLimitExceededException(
    override val message: String = "프로젝트는 최대 20개까지 저장할 수 있습니다",
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ExperienceProjectErrorCode.E400_EXPERIENCE_PROJECT_LIMIT_EXCEEDED,
    cause = cause,
)
