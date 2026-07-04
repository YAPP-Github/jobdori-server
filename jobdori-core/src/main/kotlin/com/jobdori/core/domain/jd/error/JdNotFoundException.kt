package com.jobdori.core.domain.jd.error

import com.jobdori.common.error.BaseException

data class JdNotFoundException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(message = message, errorCode = JdErrorCode.E404_JD_NOT_FOUND, cause = cause)
