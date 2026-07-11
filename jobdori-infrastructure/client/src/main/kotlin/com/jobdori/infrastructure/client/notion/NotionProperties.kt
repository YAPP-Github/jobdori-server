package com.jobdori.infrastructure.client.notion

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "notion")
data class NotionProperties(
    val clientId: String,
    val clientSecret: String,
    val apiVersion: String = "2026-03-11",
)
