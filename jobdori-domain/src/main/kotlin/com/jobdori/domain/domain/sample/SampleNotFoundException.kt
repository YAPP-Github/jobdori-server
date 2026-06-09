package com.jobdori.domain.domain.sample

import com.jobdori.common.error.BaseException

data class SampleNotFoundException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = SampleErrorCode.E404_SAMPLE_NOT_FOUND,
    cause = cause,
)
