package com.jobdori.core.application.auth

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.application.auth.oauth.google.GoogleAuthProcessor
import com.jobdori.core.application.auth.oauth.google.model.GoogleUserId
import com.jobdori.core.domain.auth.AuthToken
import com.jobdori.core.domain.auth.AuthTokenPair
import com.jobdori.core.domain.auth.service.AuthTokenProvider
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.service.UserCreator
import com.jobdori.core.domain.user.service.UserIdentityReader
import com.jobdori.core.domain.user.service.UserReader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import java.time.Instant

class AuthServiceTest : StringSpec({

    val googleAuthProcessor = mockk<GoogleAuthProcessor>()
    val userCreator = mockk<UserCreator>()
    val authTokenProvider = mockk<AuthTokenProvider>()
    val userIdentityReader = mockk<UserIdentityReader>()
    val userReader = mockk<UserReader>()

    val service = AuthService(
        googleAuthProcessor = googleAuthProcessor,
        userCreator = userCreator,
        userIdentityReader = userIdentityReader,
        userReader = userReader,
        authTokenProvider = authTokenProvider,
    )

    val command = AuthCommand(
        provider = UserIdentityProvider.GOOGLE,
        authorizationCode = "authorization-code",
        redirectUri = "https://jobdori.com/auth/callback",
    )
    val tokenPair = AuthTokenPair(
        accessToken = AuthToken(
            value = "access-token",
            tokenId = "access-token-id",
            expiresAt = Instant.parse("2030-01-01T00:30:00Z"),
        ),
        refreshToken = AuthToken(
            value = "refresh-token",
            tokenId = "refresh-token-id",
            expiresAt = Instant.parse("2030-01-15T00:00:00Z"),
        ),
    )

    "Google 사용자 ID를 조회하고 가입 처리한 뒤 JWT를 발급한다" {
        // given
        every { googleAuthProcessor.getGoogleUserId(command) } returns GoogleUserId("google-user-id")
        every {
            userCreator.create(
                provider = UserIdentityProvider.GOOGLE,
                providerUserId = "google-user-id",
            )
        } returns User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )
        every { authTokenProvider.issue("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns tokenPair

        // when & then
        service.signUp(command) shouldBe tokenPair

        // then
        verifyOrder {
            googleAuthProcessor.getGoogleUserId(command)
            userCreator.create(UserIdentityProvider.GOOGLE, "google-user-id")
            authTokenProvider.issue("3f5c9d79-2255-4b76-bd31-013cd01d49d6")
        }
    }

    "Google 식별 정보로 사용자를 조회하고 JWT를 발급한다" {
        // given
        every { googleAuthProcessor.getGoogleUserId(command) } returns GoogleUserId("google-user-id")
        every {
            userIdentityReader.findIdentity(
                provider = UserIdentityProvider.GOOGLE,
                providerUserId = "google-user-id",
            )
        } returns UserIdentity(
            id = 20L,
            userId = 10L,
            provider = UserIdentityProvider.GOOGLE,
            providerUserId = "google-user-id",
        )
        every { userReader.getUser(10L) } returns User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )
        every { authTokenProvider.issue("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns tokenPair

        // when & then
        service.login(command) shouldBe tokenPair

        // then
        verify(exactly = 1) { authTokenProvider.issue("3f5c9d79-2255-4b76-bd31-013cd01d49d6") }
    }

    "가입되지 않은 Google 사용자는 로그인할 수 없다" {
        // given
        every { googleAuthProcessor.getGoogleUserId(command) } returns GoogleUserId("google-user-id")
        every {
            userIdentityReader.findIdentity(
                provider = UserIdentityProvider.GOOGLE,
                providerUserId = "google-user-id",
            )
        } returns null

        // when & then
        shouldThrow<UserNotFoundException> {
            service.login(command)
        }

        // then
        verify(exactly = 0) { authTokenProvider.issue(any()) }
    }

})

