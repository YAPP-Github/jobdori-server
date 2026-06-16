package com.jobdori.core.application.auth

import com.jobdori.core.application.auth.command.AuthCommand
import com.jobdori.core.application.auth.oauth.google.GoogleAuthProcessor
import com.jobdori.core.application.auth.oauth.google.model.GoogleUserId
import com.jobdori.core.application.auth.token.AuthToken
import com.jobdori.core.application.auth.token.AuthTokenPair
import com.jobdori.core.application.auth.token.AuthTokenProvider
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentifyProvider
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyOrder
import java.time.Instant

class SignUpServiceTest : StringSpec({

    val googleAuthProcessor = mockk<GoogleAuthProcessor>()
    val signUpUserService = mockk<SignUpUserService>()
    val authTokenProvider = mockk<AuthTokenProvider>()
    val service = SignUpService(
        googleAuthProcessor = googleAuthProcessor,
        signUpUserService = signUpUserService,
        authTokenProvider = authTokenProvider,
    )
    val command = googleAuthCommand()

    "Google 사용자 ID를 조회하고 가입 처리한 뒤 JWT를 발급한다" {
        // given
        every { googleAuthProcessor.getGoogleUserId(command) } returns GoogleUserId("google-user-id")
        every {
            signUpUserService.signUp(
                UserIdentifyProvider.GOOGLE,
                "google-user-id",
            )
        } returns User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )
        every { authTokenProvider.issue("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns tokenPair()

        // when & then
        service.signUp(command) shouldBe tokenPair()

        // then
        verifyOrder {
            googleAuthProcessor.getGoogleUserId(command)
            signUpUserService.signUp(UserIdentifyProvider.GOOGLE, "google-user-id")
            authTokenProvider.issue("3f5c9d79-2255-4b76-bd31-013cd01d49d6")
        }
    }

})

internal fun googleAuthCommand(): AuthCommand =
    AuthCommand(
        provider = UserIdentifyProvider.GOOGLE,
        authorizationCode = "authorization-code",
    )

internal fun tokenPair(): AuthTokenPair =
    AuthTokenPair(
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
