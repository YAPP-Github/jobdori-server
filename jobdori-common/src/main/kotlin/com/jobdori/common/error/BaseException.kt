package com.jobdori.common.error

abstract class BaseException(
    override val message: String,
    open val errorCode: ErrorCode,
    override val cause: Throwable? = null,
    open val details: List<ErrorDetail> = emptyList(),
) : RuntimeException(message, cause)
