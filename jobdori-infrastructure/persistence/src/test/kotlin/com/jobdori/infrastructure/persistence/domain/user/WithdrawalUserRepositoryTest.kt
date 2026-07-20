package com.jobdori.infrastructure.persistence.domain.user

import com.jobdori.core.domain.user.WithdrawalUser
import com.jobdori.core.domain.user.repository.WithdrawalUserRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.user.repository.WithdrawalUserJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

@IntegrationTest
class WithdrawalUserRepositoryTest(
    private val withdrawalUserRepository: WithdrawalUserRepository,
    private val withdrawalUserJpaRepository: WithdrawalUserJpaRepository,
) : StringSpec({

    afterEach {
        withdrawalUserJpaRepository.deleteAll()
    }

    "탈퇴 사용자의 삭제 작업 식별 정보를 저장한다" {
        val withdrawalUser = WithdrawalUser(
            originalUserId = 10L,
            publicId = "withdrawn-user",
        )

        val saved = withdrawalUserRepository.save(withdrawalUser)

        saved.id shouldBe withdrawalUserJpaRepository.findAll().single().id
        withdrawalUserJpaRepository.findAll() shouldHaveSize 1
        withdrawalUserJpaRepository.findAll().single().also {
            it.originalUserId shouldBe 10L
            it.publicId shouldBe "withdrawn-user"
        }
    }

})
