package com.jobdori.infrastructure.client.discord

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "discord.error-notification")
data class DiscordErrorNotificationProperties(
    val enabled: Boolean = false,
    val webhookUrl: String = "",
)
