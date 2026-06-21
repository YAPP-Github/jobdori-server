package com.jobdori.infrastructure.client.oauth.google

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "google.oauth")
data class GoogleOAuthProperties(
    val clientId: String,
    val clientSecret: String,
)
