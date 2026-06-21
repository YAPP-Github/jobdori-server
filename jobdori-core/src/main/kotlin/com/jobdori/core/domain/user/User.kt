package com.jobdori.core.domain.user

import java.util.UUID

data class User(
    val id: Long,
    val publicId: String,
) {

    companion object {
        fun newInstance(
            publicId: String = UUID.randomUUID().toString(),
        ) = User(
            id = 0L,
            publicId = publicId,
        )
    }

}
