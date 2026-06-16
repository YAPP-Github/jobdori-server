package com.jobdori.infrastructure.persistence.user

import com.jobdori.core.domain.user.UserIdentifyFixture
import com.jobdori.core.domain.user.UserIdentifyProvider
import com.jobdori.core.domain.user.repository.UserIdentifyRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.user.entity.UserIdentifyEntity
import com.jobdori.infrastructure.persistence.user.repository.UserIdentifyJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

@IntegrationTest
class UserIdentifyRepositoryTest(
    private val userIdentifyRepository: UserIdentifyRepository,
    private val userIdentifyJpaRepository: UserIdentifyJpaRepository,
) : StringSpec({

    afterEach {
        userIdentifyJpaRepository.deleteAll()
    }

    "provider와 identifyId로 사용자 식별 정보를 조회한다" {
        // given
        val identifyId = "google-user-id"

        val domain = UserIdentifyFixture.create(identifyId = identifyId)
        val entity = userIdentifyJpaRepository.save(UserIdentifyEntity.from(domain))

        // when
        val result = userIdentifyRepository.findByProviderAndIdentifyId(
            provider = UserIdentifyProvider.GOOGLE,
            identifyId = identifyId,
        )

        // then
        result?.id shouldBe entity.id
        result?.userId shouldBe domain.userId
        result?.identifyProvider shouldBe UserIdentifyProvider.GOOGLE
        result?.identifyId shouldBe identifyId
    }

    "존재하지 않는 사용자 식별 정보는 null을 반환한다" {
        // when & then
        userIdentifyRepository.findByProviderAndIdentifyId(
            provider = UserIdentifyProvider.GOOGLE,
            identifyId = "unknown",
        ).shouldBeNull()
    }

    "사용자 식별 정보를 저장한다" {
        // when
        val saved = userIdentifyRepository.save(
            UserIdentifyFixture.create(
                id = 0L,
                userId = 1000L,
                identifyProvider = UserIdentifyProvider.GOOGLE,
                identifyId = "google-user-id",
            ),
        )

        // then
        val identifies = userIdentifyJpaRepository.findAll()
        identifies shouldHaveSize 1
        identifies[0].also {
            it.id shouldBe saved.id
            it.userId shouldBe saved.userId
            it.identifyProvider shouldBe saved.identifyProvider
            it.identifyId shouldBe saved.identifyId
        }
    }

})
