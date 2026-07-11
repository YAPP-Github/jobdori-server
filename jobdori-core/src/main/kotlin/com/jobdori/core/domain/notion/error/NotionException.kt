package com.jobdori.core.domain.notion.error

import com.jobdori.common.error.BaseException

open class NotionException(
    override val message: String,
    override val errorCode: NotionErrorCode,
    override val cause: Throwable? = null,
) : BaseException(
    message = message,
    errorCode = errorCode,
    cause = cause,
)
