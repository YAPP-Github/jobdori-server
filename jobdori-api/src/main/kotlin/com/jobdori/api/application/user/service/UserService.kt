package com.jobdori.api.application.user.service

import com.jobdori.api.application.user.dto.response.UserResponse
import com.jobdori.core.domain.user.service.UserReader
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userReader: UserReader,
) {

    fun getMe(userId: Long): UserResponse {
        val user = userReader.getUser(userId)
        return UserResponse.from(user)
    }

}
