package com.jobdori.infrastructure.persistence.domain.user

import com.jobdori.core.domain.user.UserFixture
import com.jobdori.core.domain.user.repository.UserRepository
import com.jobdori.infrastructure.persistence.IntegrationTest
import com.jobdori.infrastructure.persistence.domain.user.repository.UserJpaRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.jdbc.core.JdbcTemplate

@IntegrationTest
class UserRepositoryTest(
    private val userRepository: UserRepository,
    private val userJpaRepository: UserJpaRepository,
    private val jdbcTemplate: JdbcTemplate,
) : StringSpec({

    afterEach {
        userJpaRepository.deleteAll()
    }

    "ID로 사용자를 조회한다" {
        // given
        val saved = userRepository.save(UserFixture.create())

        // when & then
        userRepository.findById(saved.id) shouldBe saved
    }

    "Public ID로 사용자를 조회한다" {
        // given
        val saved = userRepository.save(UserFixture.create())

        // when & then
        userRepository.findByPublicId(saved.publicId) shouldBe saved
    }

    "존재하지 않는 사용자는 null을 반환한다" {
        // when & then
        userRepository.findById(Long.MAX_VALUE).shouldBeNull()
    }

    "사용자를 저장한다" {
        // when
        val saved = userRepository.save(UserFixture.create())

        // then
        userJpaRepository.flush()
        val users = userJpaRepository.findAll()
        users shouldHaveSize 1
        users[0].also {
            it.id shouldBe saved.id
            it.publicId shouldBe saved.publicId
            it.email shouldBe saved.email
            it.name shouldBe saved.name
            it.profileImageUrl shouldBe saved.profileImageUrl
        }
        jdbcTemplate.queryForObject(
            "select email_encrypted from user_v1 where id = ?",
            String::class.java,
            saved.id,
        ) shouldNotBe saved.email
    }

    "ID로 사용자를 삭제한다" {
        val target = userRepository.save(UserFixture.create(publicId = "target"))
        val other = userRepository.save(UserFixture.create(publicId = "other"))

        userRepository.deleteById(target.id)

        userJpaRepository.findById(target.id).isEmpty shouldBe true
        userJpaRepository.findById(other.id).isPresent shouldBe true
    }

})
