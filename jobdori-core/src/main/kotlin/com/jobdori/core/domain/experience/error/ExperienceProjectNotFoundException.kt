package com.jobdori.core.domain.experience.error

import com.jobdori.common.error.BaseException

data class ExperienceProjectNotFoundException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ExperienceProjectErrorCode.E404_EXPERIENCE_PROJECT_NOT_FOUND,
    cause = cause,
)
