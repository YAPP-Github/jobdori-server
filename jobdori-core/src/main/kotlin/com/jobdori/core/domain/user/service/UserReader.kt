package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.User
import com.jobdori.core.domain.user.error.UserNotFoundException
import com.jobdori.core.domain.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserReader(
    private val userRepository: UserRepository,
) {

    fun getUser(userId: Long): User {
        return userRepository.findById(userId)
            ?: throw UserNotFoundException("등록되지 않은 사용자($userId)입니다")
    }

    fun getUser(publicId: String): User {
        return userRepository.findByPublicId(publicId)
            ?: throw UserNotFoundException("등록되지 않은 사용자($publicId)입니다")
    }

}
