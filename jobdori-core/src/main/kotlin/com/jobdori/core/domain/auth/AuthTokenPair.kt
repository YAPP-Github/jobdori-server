package com.jobdori.core.domain.auth

data class AuthTokenPair(
    val accessToken: AuthToken,
    val refreshToken: AuthToken,
)
