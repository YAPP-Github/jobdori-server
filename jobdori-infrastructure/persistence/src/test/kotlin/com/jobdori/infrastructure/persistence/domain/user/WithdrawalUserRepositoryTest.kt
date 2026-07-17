package com.jobdori.infrastructure.persistence.domain.user

import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.WithdrawalUser
import com.jobdori.core.domain.user.WithdrawalUserIdentity
import com.jobdori.core.domain.user.repository.WithdrawalUserRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.user.repository.WithdrawalUserJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

@IntegrationTest
class WithdrawalUserRepositoryTest(
    private val withdrawalUserRepository: WithdrawalUserRepository,
    private val withdrawalUserJpaRepository: WithdrawalUserJpaRepository,
) : StringSpec({

    afterEach {
        withdrawalUserJpaRepository.deleteAll()
    }

    "탈퇴 사용자의 원본 정보와 식별 정보를 저장한다" {
        val createdAt = LocalDateTime.of(2026, 7, 1, 10, 30)
        val updatedAt = LocalDateTime.of(2026, 7, 16, 18, 45)
        val withdrawalUser = WithdrawalUser(
            originalUserId = 10L,
            publicId = "withdrawn-user",
            email = "withdrawn@example.com",
            name = "탈퇴 사용자",
            profileImageUrl = "https://example.com/profile.png",
            userIdentities = listOf(
                WithdrawalUserIdentity(UserIdentityProvider.GOOGLE, "google-user-id"),
            ),
            userCreatedAt = createdAt,
            userUpdatedAt = updatedAt,
        )

        val saved = withdrawalUserRepository.save(withdrawalUser)

        saved.id shouldBe withdrawalUserJpaRepository.findAll().single().id
        withdrawalUserJpaRepository.findAll() shouldHaveSize 1
        withdrawalUserJpaRepository.findAll().single().also {
            it.originalUserId shouldBe 10L
            it.publicId shouldBe "withdrawn-user"
            it.email shouldBe "withdrawn@example.com"
            it.name shouldBe "탈퇴 사용자"
            it.profileImageUrl shouldBe "https://example.com/profile.png"
            it.userIdentities shouldBe listOf(
                WithdrawalUserIdentity(UserIdentityProvider.GOOGLE, "google-user-id"),
            )
            it.userCreatedAt shouldBe createdAt
            it.userUpdatedAt shouldBe updatedAt
        }
    }
})
