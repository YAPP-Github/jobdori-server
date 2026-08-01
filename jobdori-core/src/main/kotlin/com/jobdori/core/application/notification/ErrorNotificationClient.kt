package com.jobdori.core.application.notification

fun interface ErrorNotificationClient {
    fun send(notification: ErrorNotification)
}
