package com.jobdori.core.domain.user

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: Long,
    val publicId: String,
    val email: String,
    val name: String,
    val profileImageUrl: String?,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
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
            createdAt = null,
            updatedAt = null,
        )
    }

}
