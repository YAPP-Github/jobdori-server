package com.jobdori.core.domain.experience.error

import com.jobdori.common.error.BaseException

data class ExperieneceEmptyImportedException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ExperienceErrorCode.E400_EMPTY_EXPERIENCES_IMPORTED,
    cause = cause,
)

