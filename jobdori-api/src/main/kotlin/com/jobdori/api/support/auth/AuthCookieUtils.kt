package com.jobdori.api.support.auth

import com.jobdori.core.application.auth.token.AuthToken
import org.springframework.http.ResponseCookie
import java.time.Instant

object AuthCookieUtils {

    const val ACCESS_TOKEN_COOKIE = "access_token"
    const val REFRESH_TOKEN_COOKIE = "refresh_token"

    fun tokenCookie(name: String, token: AuthToken): ResponseCookie =
        ResponseCookie.from(name, token.value)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(token.expiresAt.epochSecond - Instant.now().epochSecond)
            .build()

    fun expiredCookie(name: String): ResponseCookie =
        ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .maxAge(0)
            .build()

}
