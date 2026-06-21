package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserReader(
    private val userRepository: UserRepository,
) {

    @Transactional(readOnly = true)
    fun getUser(userId: Long): User {
        return userRepository.findById(userId)
            ?: throw UserNotFoundException("등록되지 않은 사용자($userId)입니다")
    }

    @Transactional(readOnly = true)
    fun getUser(publicId: String): User {
        return userRepository.findByPublicId(publicId)
            ?: throw UserNotFoundException("등록되지 않은 사용자($publicId)입니다")
    }

}
