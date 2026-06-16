package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.UserIdentifyFixture
import com.jobdori.core.domain.user.UserIdentifyProvider
import com.jobdori.core.domain.user.repository.UserIdentifyRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class UserIdentifyReaderTest : StringSpec({

    val repository = mockk<UserIdentifyRepository>()
    val reader = UserIdentifyReader(repository)

    "provider와 identifyId로 사용자 식별 정보를 조회한다" {
        // given
        val identify = UserIdentifyFixture.create(
            id = 1L,
            userId = 2L,
            identifyProvider = UserIdentifyProvider.GOOGLE,
            identifyId = "google-user-id",
        )
        every {
            repository.findByProviderAndIdentifyId(
                UserIdentifyProvider.GOOGLE,
                "google-user-id",
            )
        } returns identify

        // when & then
        reader.findIdentify(
            UserIdentifyProvider.GOOGLE,
            "google-user-id",
        ) shouldBe identify
    }

    "사용자 식별 정보가 존재하지 않으면 null을 반환한다" {
        // given
        every {
            repository.findByProviderAndIdentifyId(
                UserIdentifyProvider.GOOGLE,
                "unknown",
            )
        } returns null

        // when & then
        reader.findIdentify(
            UserIdentifyProvider.GOOGLE,
            "unknown",
        ).shouldBeNull()
    }

})
