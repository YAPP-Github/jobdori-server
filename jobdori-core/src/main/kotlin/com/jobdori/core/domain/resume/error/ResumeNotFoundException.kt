package com.jobdori.core.domain.resume.error

import com.jobdori.common.error.BaseException

data class ResumeNotFoundException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ResumeErrorCode.E404_RESUME_NOT_FOUND,
    cause = cause,
)
