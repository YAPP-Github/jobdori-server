package com.jobdori.core.domain.notion.error

class NotionPageAccessDeniedException(
    message: String,
    cause: Throwable? = null,
) : NotionException(
    message = message,
    errorCode = NotionErrorCode.PAGE_ACCESS_DENIED,
    cause = cause,
)
