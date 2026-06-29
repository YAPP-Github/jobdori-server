package com.jobdori.core.domain.ai.error

import com.jobdori.common.error.BaseException
import com.jobdori.common.error.ErrorCode

data class AiException(
    override val message: String,
    override val errorCode: ErrorCode,
    override val cause: Throwable? = null,
) : BaseException(message, errorCode, cause)
