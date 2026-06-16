package com.jobdori.core.domain.auth.service

import com.jobdori.core.domain.auth.AuthToken
import com.jobdori.core.domain.auth.AuthTokenPair
import com.jobdori.core.domain.auth.AuthTokenPayload
import com.jobdori.core.domain.auth.AuthTokenType
import java.time.Instant

interface AuthTokenProvider {

    fun issue(userPublicId: String): AuthTokenPair

    fun issue(
        userPublicId: String,
        accessTokenExpiresAt: Instant? = null,
        refreshTokenExpiresAt: Instant? = null,
    ): AuthTokenPair

    fun issueAccessToken(userPublicId: String): AuthToken

    fun issueAccessToken(
        userPublicId: String,
        expiresAt: Instant,
    ): AuthToken

    fun parse(
        token: String,
        expectedType: AuthTokenType,
    ): AuthTokenPayload

}
