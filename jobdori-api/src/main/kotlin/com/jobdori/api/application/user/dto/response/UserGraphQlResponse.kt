package com.jobdori.api.application.user.dto.response

import com.jobdori.core.domain.user.User

data class UserGraphQlResponse(
    val id: Long,
    val userId: String,
    val email: String,
    val name: String,
    val profileImageUrl: String?,
) {

    companion object {
        fun from(user: User) = UserGraphQlResponse(
            id = user.id,
            userId = user.publicId,
            email = user.email,
            name = user.name,
            profileImageUrl = user.profileImageUrl,
        )
    }

}
