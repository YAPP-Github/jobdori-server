package com.jobdori.infrastructure.persistence.domain.user

import com.jobdori.core.domain.user.UserIdentityFixture
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.repository.UserIdentityRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.user.entity.UserIdentityEntity
import com.jobdori.infrastructure.persistence.domain.user.repository.UserIdentityJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

@IntegrationTest
class UserIdentityRepositoryTest(
    private val userIdentityRepository: UserIdentityRepository,
    private val userIdentityJpaRepository: UserIdentityJpaRepository,
) : StringSpec({

    afterEach {
        userIdentityJpaRepository.deleteAll()
    }

    "provider와 providerUserId로 사용자 식별 정보를 조회한다" {
        // given
        val providerUserId = "google-user-id"

        val domain = UserIdentityFixture.create(providerUserId = providerUserId)
        val entity = userIdentityJpaRepository.save(UserIdentityEntity.from(domain))

        // when
        val result = userIdentityRepository.findByProviderAndProviderUserId(
            provider = UserIdentityProvider.GOOGLE,
            providerUserId = providerUserId,
        )

        // then
        result?.id shouldBe entity.id
        result?.userId shouldBe domain.userId
        result?.provider shouldBe UserIdentityProvider.GOOGLE
        result?.providerUserId shouldBe providerUserId
    }

    "존재하지 않는 사용자 식별 정보는 null을 반환한다" {
        // when & then
        userIdentityRepository.findByProviderAndProviderUserId(
            provider = UserIdentityProvider.GOOGLE,
            providerUserId = "unknown",
        ).shouldBeNull()
    }

    "provider와 providerUserId로 사용자 식별 정보 존재 여부를 확인한다" {
        // given
        val providerUserId = "google-user-id"
        val domain = UserIdentityFixture.create(providerUserId = providerUserId)
        userIdentityJpaRepository.save(UserIdentityEntity.from(domain))

        // when & then
        userIdentityRepository.existsByProviderAndProviderUserId(
            provider = UserIdentityProvider.GOOGLE,
            providerUserId = providerUserId,
        ) shouldBe true

        userIdentityRepository.existsByProviderAndProviderUserId(
            provider = UserIdentityProvider.GOOGLE,
            providerUserId = "unknown",
        ) shouldBe false
    }

    "사용자 식별 정보를 저장한다" {
        // when
        val saved = userIdentityRepository.save(
            UserIdentityFixture.create(
                id = 0L,
                userId = 1000L,
                provider = UserIdentityProvider.GOOGLE,
                providerUserId = "google-user-id",
            ),
        )

        // then
        val identities = userIdentityJpaRepository.findAll()
        identities shouldHaveSize 1
        identities[0].also {
            it.id shouldBe saved.id
            it.userId shouldBe saved.userId
            it.provider shouldBe saved.provider
            it.providerUserId shouldBe saved.providerUserId
        }
    }

})
