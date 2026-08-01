package com.jobdori.api.support.notification

import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.application.notification.ErrorNotification
import com.jobdori.core.application.notification.ErrorNotificationClient
import org.springframework.beans.factory.ObjectProvider
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AsyncErrorNotificationSender(
    private val clientProvider: ObjectProvider<ErrorNotificationClient>,
) {

    @Async(ErrorNotificationConfig.ERROR_NOTIFICATION_EXECUTOR)
    fun send(notification: ErrorNotification) {
        val client = clientProvider.ifAvailable ?: return
        runCatching { client.send(notification) }
            .onFailure { log.warn(it) { "Discord error notification failed" } }
    }
}
