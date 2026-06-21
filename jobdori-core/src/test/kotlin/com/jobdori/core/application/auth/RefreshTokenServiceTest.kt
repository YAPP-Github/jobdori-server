package com.jobdori.core.application.auth

import com.jobdori.core.domain.auth.error.InvalidAuthTokenException
import com.jobdori.core.domain.auth.AuthToken
import com.jobdori.core.domain.auth.AuthTokenPayload
import com.jobdori.core.domain.auth.service.AuthTokenProvider
import com.jobdori.core.domain.auth.AuthTokenType
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.service.UserReader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant

class RefreshAccessTokenServiceTest : StringSpec({

    val authTokenProvider = mockk<AuthTokenProvider>()
    val userReader = mockk<UserReader>()
    val service = RefreshTokenService(authTokenProvider, userReader)

    fun refreshTokenPayload(userPublicId: String) = AuthTokenPayload(
        userId = userPublicId,
        tokenId = "refresh-token-id",
        type = AuthTokenType.REFRESH,
        issuedAt = Instant.parse("2030-01-01T00:00:00Z"),
        expiresAt = Instant.parse("2030-01-15T00:00:00Z"),
    )

    "Refresh 토큰으로 Access 토큰을 재발급한다" {
        // given
        val accessToken = AuthToken(
            value = "new-access-token",
            tokenId = "new-access-token-id",
            expiresAt = Instant.parse("2030-01-01T00:30:00Z"),
        )
        every {
            authTokenProvider.parse("refresh-token", AuthTokenType.REFRESH)
        } returns refreshTokenPayload(userPublicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6")
        every { userReader.getUser("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )
        every { authTokenProvider.issueAccessToken("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns accessToken

        // when & then
        service.refresh("refresh-token") shouldBe accessToken

        // then
        verify(exactly = 1) {
            authTokenProvider.issueAccessToken("3f5c9d79-2255-4b76-bd31-013cd01d49d6")
        }
    }

    "Refresh 토큰의 사용자가 존재하지 않으면 인증 실패로 처리한다" {
        // given
        every {
            authTokenProvider.parse("refresh-token", AuthTokenType.REFRESH)
        } returns refreshTokenPayload(userPublicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6")
        every {
            userReader.getUser("3f5c9d79-2255-4b76-bd31-013cd01d49d6")
        } throws UserNotFoundException("등록되지 않은 사용자입니다")

        // when & then
        shouldThrow<InvalidAuthTokenException> {
            service.refresh("refresh-token")
        }

        // then
        verify(exactly = 0) { authTokenProvider.issueAccessToken(any()) }
    }

})
