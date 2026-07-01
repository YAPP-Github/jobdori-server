package com.jobdori.core.domain.experience.error

import com.jobdori.common.error.BaseException

data class ExperienceNotFoundException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ExperienceErrorCode.E404_EXPERIENCE_NOT_FOUND,
    cause = cause,
)
