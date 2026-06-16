package com.jobdori.core.application.auth.token

data class AuthTokenPair(
    val accessToken: AuthToken,
    val refreshToken: AuthToken,
)
