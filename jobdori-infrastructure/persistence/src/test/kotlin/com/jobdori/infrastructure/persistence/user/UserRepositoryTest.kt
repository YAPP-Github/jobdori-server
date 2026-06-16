package com.jobdori.infrastructure.persistence.user

import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserFixture
import com.jobdori.core.domain.user.repository.UserRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.user.entity.UserEntity
import com.jobdori.infrastructure.persistence.user.repository.UserJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

@IntegrationTest
class UserRepositoryTest(
    private val userRepository: UserRepository,
    private val userJpaRepository: UserJpaRepository,
) : StringSpec({

    afterEach {
        userJpaRepository.deleteAll()
    }

    "ID로 사용자를 조회한다" {
        // given
        val entity = userJpaRepository.save(UserEntity.from(UserFixture.create()))

        // when & then
        userRepository.findById(entity.id) shouldBe User(
            id = entity.id,
            publicId = entity.publicId,
        )
    }

    "Public ID로 사용자를 조회한다" {
        // given
        val entity = userJpaRepository.save(UserEntity.from(UserFixture.create()))

        // when & then
        userRepository.findByPublicId(entity.publicId) shouldBe User(
            id = entity.id,
            publicId = entity.publicId,
        )
    }

    "존재하지 않는 사용자는 null을 반환한다" {
        // when & then
        userRepository.findById(Long.MAX_VALUE).shouldBeNull()
    }

    "사용자를 저장한다" {
        // when
        val saved = userRepository.save(UserFixture.create())

        // then
        val users = userJpaRepository.findAll()
        users shouldHaveSize 1
        users[0].also {
            it.id shouldBe saved.id
            it.publicId shouldBe saved.publicId
        }
    }

})
