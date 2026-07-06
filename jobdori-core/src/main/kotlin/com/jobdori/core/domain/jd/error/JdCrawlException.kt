package com.jobdori.core.domain.jd.error

import com.jobdori.common.error.BaseException
import com.jobdori.common.error.ErrorCode

data class JdCrawlException(
    override val message: String,
    override val errorCode: ErrorCode,
    override val cause: Throwable? = null,
) : BaseException(message = message, errorCode = errorCode, cause = cause)
