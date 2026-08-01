package com.jobdori.api.support.notification

import com.jobdori.core.application.notification.ErrorNotification
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class AsyncErrorNotifier(
    private val sender: AsyncErrorNotificationSender,
    environment: Environment,
) {
    private val environmentName = environment.activeProfiles.firstOrNull() ?: "default"

    fun notify(errorCode: String, throwable: Throwable) {
        sender.send(
            ErrorNotification(
                environment = environmentName,
                errorCode = errorCode,
                exceptionType = throwable::class.qualifiedName ?: throwable::class.simpleName.orEmpty(),
                message = throwable.message.orEmpty().take(MAX_MESSAGE_LENGTH),
                stackTrace = throwable.stackTraceToString().take(MAX_STACK_TRACE_LENGTH),
            )
        )
    }

    private companion object {
        const val MAX_MESSAGE_LENGTH = 500
        const val MAX_STACK_TRACE_LENGTH = 4_000
    }
}
