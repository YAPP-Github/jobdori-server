package com.jobdori.api.application.user.controller

import com.jobdori.api.application.user.dto.response.UserResponse
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.domain.user.service.UserReader
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class UserQueryResolver(
    private val userReader: UserReader,
) {

    @QueryMapping
    fun myUser(
        @UserId userId: Long,
    ): UserResponse {
        val user = userReader.getUser(userId)
        return UserResponse.from(user)
    }

}
