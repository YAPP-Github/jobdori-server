package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.UserIdentityFixture
import com.jobdori.core.domain.user.repository.UserIdentityRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class UserIdentityReaderTest : StringSpec({

    val repository = mockk<UserIdentityRepository>()
    val reader = UserIdentityReader(repository)

    "provider와 providerUserId로 사용자 식별 정보를 조회한다" {
        // given
        val identity = UserIdentityFixture.create(
            id = 1L,
            userId = 2L,
            provider = UserIdentityProvider.GOOGLE,
            providerUserId = "google-user-id",
        )
        every {
            repository.findByProviderAndProviderUserId(
                UserIdentityProvider.GOOGLE,
                "google-user-id",
            )
        } returns identity

        // when & then
        reader.findIdentity(
            UserIdentityProvider.GOOGLE,
            "google-user-id",
        ) shouldBe identity
    }

    "사용자 식별 정보가 존재하지 않으면 null을 반환한다" {
        // given
        every {
            repository.findByProviderAndProviderUserId(
                UserIdentityProvider.GOOGLE,
                "unknown",
            )
        } returns null

        // when & then
        reader.findIdentity(
            UserIdentityProvider.GOOGLE,
            "unknown",
        ).shouldBeNull()
    }

})
