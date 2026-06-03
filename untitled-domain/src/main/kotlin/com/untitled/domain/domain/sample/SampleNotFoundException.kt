package com.untitled.domain.domain.sample

import com.untitled.common.error.BaseException

data class SampleNotFoundException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = SampleErrorCode.E404_SAMPLE_NOT_FOUND,
    cause = cause,
)
