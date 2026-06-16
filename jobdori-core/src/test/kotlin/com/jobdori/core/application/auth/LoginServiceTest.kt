package com.jobdori.core.application.auth

import com.jobdori.core.application.auth.oauth.google.GoogleAuthProcessor
import com.jobdori.core.application.auth.oauth.google.model.GoogleUserId
import com.jobdori.core.application.auth.token.AuthTokenProvider
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentify
import com.jobdori.core.domain.user.UserIdentifyProvider
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.service.UserIdentifyReader
import com.jobdori.core.domain.user.service.UserReader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class LoginServiceTest : StringSpec({

    val googleAuthProcessor = mockk<GoogleAuthProcessor>()
    val userIdentifyReader = mockk<UserIdentifyReader>()
    val userReader = mockk<UserReader>()
    val authTokenProvider = mockk<AuthTokenProvider>()
    val service = LoginService(
        googleAuthProcessor = googleAuthProcessor,
        userIdentifyReader = userIdentifyReader,
        userReader = userReader,
        authTokenProvider = authTokenProvider,
    )
    val command = googleAuthCommand()

    "Google 식별 정보로 사용자를 조회하고 JWT를 발급한다" {
        // given
        every { googleAuthProcessor.getGoogleUserId(command) } returns GoogleUserId("google-user-id")
        every {
            userIdentifyReader.findIdentify(
                UserIdentifyProvider.GOOGLE,
                "google-user-id",
            )
        } returns UserIdentify(
            id = 20L,
            userId = 10L,
            identifyProvider = UserIdentifyProvider.GOOGLE,
            identifyId = "google-user-id",
        )
        every { userReader.getUser(10L) } returns User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )
        every { authTokenProvider.issue("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns tokenPair()

        // when & then
        service.login(command) shouldBe tokenPair()

        // then
        verify(exactly = 1) { authTokenProvider.issue("3f5c9d79-2255-4b76-bd31-013cd01d49d6") }
    }

    "가입되지 않은 Google 사용자는 로그인할 수 없다" {
        // given
        every { googleAuthProcessor.getGoogleUserId(command) } returns GoogleUserId("google-user-id")
        every {
            userIdentifyReader.findIdentify(
                UserIdentifyProvider.GOOGLE,
                "google-user-id",
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
