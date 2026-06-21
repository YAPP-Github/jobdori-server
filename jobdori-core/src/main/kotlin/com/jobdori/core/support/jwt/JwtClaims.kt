package com.jobdori.core.support.jwt

import java.time.Instant

data class JwtClaims(
    val subject: String,
    val tokenId: String,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val customClaims: Map<String, String> = emptyMap(),
)
