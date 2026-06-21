package com.jobdori.api.support.auth

import com.jobdori.core.domain.auth.error.InvalidAuthTokenException

object BearerTokenExtractor {

    fun extract(authorization: String?): String {
        val parts = authorization?.trim()?.split(BEARER_PREFIX, limit = 2)
            ?: throw InvalidAuthTokenException("Authorization 헤더가 없습니다")

        if (parts.size != 2 || parts[0].isNotEmpty() || parts[1].isBlank()) {
            throw InvalidAuthTokenException("Authorization 헤더는 Bearer 토큰 형식이어야 합니다")
        }

        return parts[1]
    }

    private const val BEARER_PREFIX = "Bearer "

}
