package com.jobdori.core.domain.user.error

import com.jobdori.common.error.BaseException

data class UserNotFoundException(
    override val message: String,
    override val errorCode: UserErrorCode = UserErrorCode.E404_USER_NOT_FOUND,
    override val cause: Throwable? = null,
) : BaseException(message = message, errorCode = errorCode, cause = cause)
