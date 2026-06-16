package com.jobdori.core.application.auth.error

import com.jobdori.common.error.BaseException
import com.jobdori.common.error.CommonErrorCode

class InvalidAuthTokenException(
    override val message: String,
    override val errorCode: CommonErrorCode = CommonErrorCode.E401_INVALID_AUTH_TOKEN,
    override val cause: Throwable? = null,
) : BaseException(message = message, errorCode = errorCode, cause = cause)
