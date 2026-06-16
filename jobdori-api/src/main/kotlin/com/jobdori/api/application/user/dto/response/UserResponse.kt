package com.jobdori.api.application.user.dto.response

import com.jobdori.core.domain.user.User

data class UserResponse(
    val userId: String,
) {

    companion object {
        fun from(user: User) = UserResponse(
            userId = user.publicId,
        )
    }

}
