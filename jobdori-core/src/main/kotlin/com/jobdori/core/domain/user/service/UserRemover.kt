package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.WithdrawalUser
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.repository.UserIdentityRepository
import com.jobdori.core.domain.user.repository.UserRepository
import com.jobdori.core.domain.user.repository.WithdrawalUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserRemover(
    private val userRepository: UserRepository,
    private val userIdentityRepository: UserIdentityRepository,
    private val withdrawalUserRepository: WithdrawalUserRepository,
) {

    @Transactional
    fun remove(userId: Long) {
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException("등록되지 않은 사용자($userId)입니다")
        val identities = userIdentityRepository.findAllByUserId(userId)

        withdrawalUserRepository.save(WithdrawalUser.from(user, identities))
        userIdentityRepository.deleteAllByUserId(userId)
        userRepository.deleteById(userId)
    }

}
