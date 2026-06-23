package com.jobdori.core.domain.user

import java.util.UUID

data class User(
    val id: Long,
    val publicId: String,
    val name: String,
    val profileImageUrl: String?,
) {

    companion object {
        fun newInstance(
            publicId: String = UUID.randomUUID().toString(),
            name: String,
            profileImageUrl: String?,
        ) = User(
            id = 0L,
            publicId = publicId,
            name = name,
            profileImageUrl = profileImageUrl,
        )
    }

}
