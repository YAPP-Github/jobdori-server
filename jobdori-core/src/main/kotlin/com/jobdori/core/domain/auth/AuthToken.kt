package com.jobdori.core.domain.auth

import java.time.Instant

data class AuthToken(
    val value: String,
    val tokenId: String,
    val expiresAt: Instant,
)
