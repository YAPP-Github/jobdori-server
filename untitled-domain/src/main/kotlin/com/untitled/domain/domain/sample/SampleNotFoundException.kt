package com.untitled.domain.domain.sample

import com.untitled.common.error.BaseException
import com.untitled.common.error.ErrorCode

data class SampleNotFoundException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ErrorCode.E404_SAMPLE_NOT_FOUND,
    cause = cause,
)
