package com.jobdori.core.application.auth.token

import java.time.Instant

data class AuthToken(
    val value: String,
    val tokenId: String,
    val expiresAt: Instant,
)
