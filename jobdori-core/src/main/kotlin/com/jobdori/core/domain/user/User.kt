package com.jobdori.core.domain.user

import java.util.UUID

data class User(
    val id: Long,
    val publicId: String,
    val email: String,
    val name: String,
    val profileImageUrl: String?,
) {

    companion object {
        fun newInstance(
            publicId: String = UUID.randomUUID().toString(),
            email: String,
            name: String,
            profileImageUrl: String?,
        ) = User(
            id = 0L,
            publicId = publicId,
            email = email,
            name = name,
            profileImageUrl = profileImageUrl,
        )
    }

}
