package com.jobdori.core.domain.notion.error

class NotionConnectionNotFoundException(
    message: String,
) : NotionException(
    message = message,
    errorCode = NotionErrorCode.CONNECTION_NOT_FOUND,
)
