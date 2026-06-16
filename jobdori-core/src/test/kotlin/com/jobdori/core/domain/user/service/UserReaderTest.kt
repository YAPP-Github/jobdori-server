package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.UserFixture
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class UserReaderTest : StringSpec({

    val userRepository = mockk<UserRepository>()
    val userReader = UserReader(userRepository)

    "ID로 사용자를 조회한다" {
        // given
        val user = UserFixture.create(id = 1L)
        every { userRepository.findById(1L) } returns user

        // when & then
        userReader.getUser(1L) shouldBe user
    }

    "사용자가 존재하지 않으면 예외를 던진다" {
        // given
        every { userRepository.findById(1L) } returns null

        // when & then
        shouldThrow<UserNotFoundException> {
            userReader.getUser(1L)
        }
    }

    "Public ID로 사용자를 조회한다" {
        // given
        val user = UserFixture.create(
            id = 1L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
        )
        every { userRepository.findByPublicId("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns user

        // when & then
        userReader.getUser("3f5c9d79-2255-4b76-bd31-013cd01d49d6") shouldBe user
    }

})
