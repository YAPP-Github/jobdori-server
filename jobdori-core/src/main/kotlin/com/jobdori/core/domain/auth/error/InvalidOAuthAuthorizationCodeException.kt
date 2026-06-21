package com.jobdori.core.domain.auth.error

import com.jobdori.common.error.BaseException

data class InvalidOAuthAuthorizationCodeException(
    override val message: String,
    override val errorCode: AuthErrorCode = AuthErrorCode.INVALID_OAUTH_AUTHORIZATION_CODE,
    override val cause: Throwable? = null,
) : BaseException(message = message, errorCode = errorCode, cause = cause)
