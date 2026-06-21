package com.jobdori.api.application.user.controller

import com.jobdori.api.application.user.dto.response.UserResponse
import com.jobdori.api.support.auth.Authenticated
import com.jobdori.api.support.auth.UserId
import com.jobdori.api.support.rest.ApiResponse
import com.jobdori.core.domain.user.service.UserReader
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userReader: UserReader,
) {

    @GetMapping("/v1/users/me")
    @Authenticated
    fun getMyUser(
        @UserId userId: Long,
    ): ApiResponse<UserResponse> {
        val user = userReader.getUser(userId)
        return ApiResponse.ok(UserResponse.from(user))
    }

}
