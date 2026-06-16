package com.jobdori.core.domain.auth.error

import com.jobdori.common.error.BaseException
import com.jobdori.common.error.CommonErrorCode

data class AuthTokenExpiredException(
    override val message: String,
    override val errorCode: CommonErrorCode = CommonErrorCode.E401_TOKEN_EXPIRED,
    override val cause: Throwable? = null,
) : BaseException(message = message, errorCode = errorCode, cause = cause)
