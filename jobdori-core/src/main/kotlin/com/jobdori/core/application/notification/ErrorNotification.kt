package com.jobdori.core.application.notification

data class ErrorNotification(
    val environment: String,
    val errorCode: String,
    val exceptionType: String,
    val message: String,
    val stackTrace: String,
)
