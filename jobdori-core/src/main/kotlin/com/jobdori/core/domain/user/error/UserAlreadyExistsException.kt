package com.jobdori.core.domain.user.error

import com.jobdori.common.error.BaseException

data class UserAlreadyExistsException(
    override val message: String,
    override val errorCode: UserErrorCode = UserErrorCode.E409_USER_ALREADY_EXISTS,
    override val cause: Throwable? = null,
) : BaseException(message = message, errorCode = errorCode, cause = cause)
