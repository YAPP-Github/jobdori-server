package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.WithdrawalUser
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.repository.UserIdentityRepository
import com.jobdori.core.domain.user.repository.UserRepository
import com.jobdori.core.domain.user.repository.WithdrawalUserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.confirmVerified
import io.mockk.verify
import io.mockk.verifyOrder

class UserRemoverTest : StringSpec({
    val userRepository = mockk<UserRepository>()
    val userIdentityRepository = mockk<UserIdentityRepository>()
    val withdrawalUserRepository = mockk<WithdrawalUserRepository>()
    val manager = UserRemover(userRepository, userIdentityRepository, withdrawalUserRepository)

    "사용자를 백업한 뒤 식별 정보와 사용자를 삭제한다" {
        val user = User(1L, "public-id", "user@example.com", "홍길동", null)
        val identity = UserIdentity(2L, 1L, UserIdentityProvider.GOOGLE, "google-id")
        every { userRepository.findById(1L) } returns user
        every { userIdentityRepository.findAllByUserId(1L) } returns listOf(identity)
        every { withdrawalUserRepository.save(any()) } answers { firstArg<WithdrawalUser>().copy(id = 3L) }
        justRun { userIdentityRepository.deleteAllByUserId(1L) }
        justRun { userRepository.deleteById(1L) }

        manager.remove(1L)

        verifyOrder {
            userRepository.findById(1L)
            userIdentityRepository.findAllByUserId(1L)
            withdrawalUserRepository.save(WithdrawalUser.from(user, listOf(identity)))
            userIdentityRepository.deleteAllByUserId(1L)
            userRepository.deleteById(1L)
        }
    }

    "존재하지 않는 사용자는 탈퇴할 수 없다" {
        every { userRepository.findById(2L) } returns null

        shouldThrow<UserNotFoundException> {
            manager.remove(2L)
        }

        verify(exactly = 1) { userRepository.findById(2L) }
        confirmVerified(userIdentityRepository, withdrawalUserRepository)
    }
})
