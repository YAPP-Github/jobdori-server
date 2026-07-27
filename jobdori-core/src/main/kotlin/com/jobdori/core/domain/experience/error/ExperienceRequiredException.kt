package com.jobdori.core.domain.experience.error

import com.jobdori.common.error.BaseException

data class ExperienceRequiredException(
    override val message: String = ExperienceErrorCode.E422_EXPERIENCE_REQUIRED.message,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ExperienceErrorCode.E422_EXPERIENCE_REQUIRED,
    cause = cause,
)
