package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.error.UserAlreadyExistsException
import com.jobdori.core.domain.user.repository.UserIdentityRepository
import com.jobdori.core.domain.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class UserCreatorTest : StringSpec({

    val userRepository = mockk<UserRepository>()
    val userIdentityRepository = mockk<UserIdentityRepository>()
    val service = UserCreator(
        userRepository = userRepository,
        userIdentityRepository = userIdentityRepository,
    )

    "새로운 유저를 등록한다" {
        // given
        every {
            userIdentityRepository.existsByProviderAndProviderUserId(
                UserIdentityProvider.GOOGLE,
                "google-user-id",
            )
        } returns false
        val userSlot = slot<User>()
        every { userRepository.save(capture(userSlot)) } returns User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            email = "hong@example.com",
            name = "홍길동",
            profileImageUrl = "https://lh3.googleusercontent.com/profile",
        )
        every {
            userIdentityRepository.save(
                UserIdentity(
                    id = 0L,
                    userId = 10L,
                    provider = UserIdentityProvider.GOOGLE,
                    providerUserId = "google-user-id",
                ),
            )
        } returns UserIdentity(
            id = 20L,
            userId = 10L,
            provider = UserIdentityProvider.GOOGLE,
            providerUserId = "google-user-id",
        )

        // when & then
        service.create(
            provider = UserIdentityProvider.GOOGLE,
            providerUserId = "google-user-id",
            email = "hong@example.com",
            name = "홍길동",
            profileImageUrl = "https://lh3.googleusercontent.com/profile",
        ) shouldBe User(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            email = "hong@example.com",
            name = "홍길동",
            profileImageUrl = "https://lh3.googleusercontent.com/profile",
        )

        // then
        userSlot.captured.id shouldBe 0L
        userSlot.captured.email shouldBe "hong@example.com"
        userSlot.captured.name shouldBe "홍길동"
        userSlot.captured.profileImageUrl shouldBe "https://lh3.googleusercontent.com/profile"
        verify(exactly = 1) { userRepository.save(any()) }
        verify(exactly = 1) { userIdentityRepository.save(any()) }
    }

    "이미 가입된 식별 정보면 유저를 저장하지 않는다" {
        // given
        every {
            userIdentityRepository.existsByProviderAndProviderUserId(
                provider = UserIdentityProvider.GOOGLE,
                providerUserId = "google-user-id",
            )
        } returns true

        // when & then
        shouldThrow<UserAlreadyExistsException> {
            service.create(
                provider = UserIdentityProvider.GOOGLE,
                providerUserId = "google-user-id",
                email = "hong@example.com",
                name = "홍길동",
                profileImageUrl = "https://lh3.googleusercontent.com/profile",
            )
        }

        // then
        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 0) { userIdentityRepository.save(any()) }
    }

})
