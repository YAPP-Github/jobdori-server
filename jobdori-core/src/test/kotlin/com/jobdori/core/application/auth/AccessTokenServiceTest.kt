package com.jobdori.core.application.auth

import com.jobdori.core.domain.auth.AuthTokenPayload
import com.jobdori.core.domain.auth.AuthTokenType
import com.jobdori.core.domain.auth.error.InvalidAuthTokenException
import com.jobdori.core.domain.auth.service.AuthTokenProvider
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

class AuthUserReaderTest : StringSpec({

    val authTokenProvider = mockk<AuthTokenProvider>()
    val userReader = mockk<UserReader>()
    val accessTokenService = AccessTokenService(authTokenProvider, userReader)

    "Access 토큰의 사용자 존재를 검증하고 userId를 반환한다" {
        // given
        every {
            authTokenProvider.parse("access-token", AuthTokenType.ACCESS)
        } returns tokenPayload(userPublicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6")
        every { userReader.getUser("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )

        // when & then
        accessTokenService.getUserId("access-token") shouldBe 10L

        // then
        verify(exactly = 1) { userReader.getUser("3f5c9d79-2255-4b76-bd31-013cd01d49d6") }
    }

    "토큰의 사용자가 존재하지 않으면 인증 실패로 처리한다" {
        // given
        every {
            authTokenProvider.parse("access-token", AuthTokenType.ACCESS)
        } returns tokenPayload(userPublicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6")
        every {
            userReader.getUser("3f5c9d79-2255-4b76-bd31-013cd01d49d6")
        } throws UserNotFoundException("등록되지 않은 사용자입니다")

        // when & then
        shouldThrow<InvalidAuthTokenException> {
            accessTokenService.getUserId("access-token")
        }
    }

})

private fun tokenPayload(userPublicId: String): AuthTokenPayload =
    AuthTokenPayload(
        userId = userPublicId,
        tokenId = "token-id",
        type = AuthTokenType.ACCESS,
        issuedAt = Instant.now(),
        expiresAt = Instant.now().plusSeconds(60),
    )
