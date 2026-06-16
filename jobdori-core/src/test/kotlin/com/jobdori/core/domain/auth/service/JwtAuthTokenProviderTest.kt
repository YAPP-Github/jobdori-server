package com.jobdori.core.domain.auth.service

import com.jobdori.core.domain.auth.AuthTokenProperties
import com.jobdori.core.domain.auth.AuthTokenType
import com.jobdori.core.domain.auth.error.InvalidAuthTokenException
import com.jobdori.core.support.jwt.HmacSha256JwtCodec
import com.jobdori.core.support.jwt.JwtCodecProperties
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Duration
import java.time.Instant
import java.util.Base64

class JwtAuthTokenProviderTest : StringSpec({

    val jwtCodec = HmacSha256JwtCodec(
        properties = JwtCodecProperties(
            secret = Base64.getEncoder().encodeToString(ByteArray(32) { it.toByte() }),
        ),
    )
    val provider = JwtAuthTokenProvider(
        jwtCodec = jwtCodec,
        properties = AuthTokenProperties(
            accessTokenTtl = Duration.ofMinutes(30),
            refreshTokenTtl = Duration.ofDays(14),
        ),
    )

    "Access 토큰과 Refresh 토큰을 발급하고 파싱한다" {
        // given
        val userId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6"

        // when
        val pair = provider.issue(userPublicId = userId)

        // then
        pair.accessToken.value shouldNotBe pair.refreshToken.value
        pair.accessToken.tokenId shouldNotBe pair.refreshToken.tokenId

        provider.parse(
            pair.accessToken.value,
            AuthTokenType.ACCESS,
        ).userId shouldBe userId

        provider.parse(
            pair.refreshToken.value,
            AuthTokenType.REFRESH,
        ).userId shouldBe userId
    }

    "기대 토큰 종류와 실제 토큰 종류가 다르면 거부한다" {
        // given
        val pair = provider.issue(userPublicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6")

        // when & then
        shouldThrow<InvalidAuthTokenException> {
            provider.parse(
                pair.accessToken.value,
                AuthTokenType.REFRESH,
            )
        }
    }

    "Access 토큰만 발급한다" {
        // given
        val userId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6"

        // when
        val accessToken = provider.issueAccessToken(userPublicId = userId)

        // then
        provider.parse(
            accessToken.value,
            AuthTokenType.ACCESS,
        ).userId shouldBe userId
    }

    "지정한 만료 시간으로 Access 토큰을 발급한다" {
        // given
        val userId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6"
        val expiresAt = Instant.parse("2030-01-01T00:00:00Z")

        // when
        val accessToken = provider.issueAccessToken(
            userPublicId = userId,
            expiresAt = expiresAt,
        )
        val payload = provider.parse(
            accessToken.value,
            AuthTokenType.ACCESS,
        )

        // then
        accessToken.expiresAt shouldBe expiresAt
        payload.expiresAt shouldBe expiresAt
    }

    "빈 사용자 ID로 토큰을 발급하지 않는다" {
        // when & then
        shouldThrow<IllegalArgumentException> {
            provider.issue(userPublicId = "")
        }

        shouldThrow<IllegalArgumentException> {
            provider.issueAccessToken(userPublicId = "")
        }
    }

})
