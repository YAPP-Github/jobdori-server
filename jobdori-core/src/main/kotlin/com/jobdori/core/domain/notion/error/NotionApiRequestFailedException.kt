package com.jobdori.core.domain.notion.error

class NotionApiRequestFailedException(
    message: String,
    cause: Throwable? = null,
) : NotionException(
    message = message,
    errorCode = NotionErrorCode.API_REQUEST_FAILED,
    cause = cause,
)
