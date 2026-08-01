package com.jobdori.infrastructure.client.discord

import com.jobdori.core.application.notification.ErrorNotification
import com.jobdori.core.application.notification.ErrorNotificationClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.Duration

@Component
@ConditionalOnProperty(prefix = "discord.error-notification", name = ["enabled"], havingValue = "true")
class DiscordErrorNotificationClientImpl(
    properties: DiscordErrorNotificationProperties,
) : ErrorNotificationClient {

    private val webhookUrl = properties.webhookUrl.also {
        require(it.isNotBlank()) {
            "DISCORD_ERROR_WEBHOOK_URL must be set when Discord error notification is enabled"
        }
    }
    private val restClient = RestClient.builder()
        .requestFactory(SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(Duration.ofSeconds(2))
            setReadTimeout(Duration.ofSeconds(3))
        })
        .build()

    override fun send(notification: ErrorNotification) {
        restClient.post()
            .uri(webhookUrl)
            .contentType(MediaType.APPLICATION_JSON)
            .body(mapOf("content" to notification.toDiscordContent()))
            .retrieve()
            .toBodilessEntity()
    }

    private fun ErrorNotification.toDiscordContent(): String {
        val context = listOfNotNull(
            "environment" to environment,
            "errorCode" to errorCode,
        ).joinToString("\n") { (key, value) -> "$key: ${value.sanitize()}" }

        val body = """
            🚨 **Jobdori server error**
            $context
            exception: ${exceptionType.sanitize()}
            message: ${message.sanitize()}
            ```
            ${stackTrace.sanitize()}
            ```
        """.trimIndent()

        return body.take(DISCORD_CONTENT_LIMIT)
    }

    private fun String.sanitize(): String = replace("```", "''' ")

    private companion object {
        const val DISCORD_CONTENT_LIMIT = 2_000
    }
}
