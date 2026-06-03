package com.untitled.common.error

abstract class BaseException(
    override val message: String,
    open val errorCode: ErrorCode,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
