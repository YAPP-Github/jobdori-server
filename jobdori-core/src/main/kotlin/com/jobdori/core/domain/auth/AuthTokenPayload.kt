package com.jobdori.core.domain.auth

import java.time.Instant

data class AuthTokenPayload(
    val userId: String,
    val tokenId: String,
    val type: AuthTokenType,
    val issuedAt: Instant,
    val expiresAt: Instant,
)
