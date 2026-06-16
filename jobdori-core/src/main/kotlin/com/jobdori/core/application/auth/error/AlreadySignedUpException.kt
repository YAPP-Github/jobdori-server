package com.jobdori.core.application.auth.error

import com.jobdori.common.error.BaseException

class AlreadySignedUpException(
    override val message: String,
    override val errorCode: AuthErrorCode = AuthErrorCode.E409_ALREADY_SIGNED_UP,
    override val cause: Throwable? = null,
) : BaseException(message = message, errorCode = errorCode, cause = cause)
