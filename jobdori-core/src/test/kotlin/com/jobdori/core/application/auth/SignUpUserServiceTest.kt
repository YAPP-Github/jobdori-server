package com.jobdori.core.application.auth

import com.jobdori.core.application.auth.error.AlreadySignedUpException
import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentify
import com.jobdori.core.domain.user.UserIdentifyProvider
import com.jobdori.core.domain.user.repository.UserIdentifyRepository
import com.jobdori.core.domain.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.slot

class SignUpUserServiceTest : StringSpec({

    val userRepository = mockk<UserRepository>()
    val userIdentifyRepository = mockk<UserIdentifyRepository>()
    val service = SignUpUserService(
        userRepository = userRepository,
        userIdentifyRepository = userIdentifyRepository,
    )

    "사용자와 식별 정보를 저장한다" {
        // given
        every {
            userIdentifyRepository.findByProviderAndIdentifyId(
                UserIdentifyProvider.GOOGLE,
                "google-user-id",
            )
        } returns null
        val userSlot = slot<User>()
        every { userRepository.save(capture(userSlot)) } returns User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )
        every {
            userIdentifyRepository.save(
                UserIdentify(
                    id = 0L,
                    userId = 10L,
                    identifyProvider = UserIdentifyProvider.GOOGLE,
                    identifyId = "google-user-id",
                ),
            )
        } returns UserIdentify(
            id = 20L,
            userId = 10L,
            identifyProvider = UserIdentifyProvider.GOOGLE,
            identifyId = "google-user-id",
        )

        // when & then
        service.signUp(
            provider = UserIdentifyProvider.GOOGLE,
            identifyId = "google-user-id",
        ) shouldBe User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )

        // then
        userSlot.captured.id shouldBe 0L
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { userIdentifyRepository.save(any()) }
    }

    "이미 가입된 식별 정보면 사용자를 저장하지 않는다" {
        // given
        every {
            userIdentifyRepository.findByProviderAndIdentifyId(
                UserIdentifyProvider.GOOGLE,
                "google-user-id",
            )
        } returns UserIdentify(
            id = 20L,
            userId = 10L,
            identifyProvider = UserIdentifyProvider.GOOGLE,
            identifyId = "google-user-id",
        )

        // when & then
        shouldThrow<AlreadySignedUpException> {
            service.signUp(
                provider = UserIdentifyProvider.GOOGLE,
                identifyId = "google-user-id",
            )
        }

        // then
        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userIdentifyRepository.save(any()) }
    }

})
