package com.untitled.common.error

data class InvalidArgumentsException(
    override val message: String,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = ErrorCode.E400_INVALID_ARGUMENTS,
    cause = cause,
)
