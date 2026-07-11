package com.jobdori.core.domain.notion.error

class NotionUnauthorizedException(
    message: String,
    cause: Throwable? = null,
) : NotionException(
    message = message,
    errorCode = NotionErrorCode.CONNECTION_NEED_RECONNECT,
    cause = cause,
)
