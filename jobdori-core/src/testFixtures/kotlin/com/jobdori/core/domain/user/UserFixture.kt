package com.jobdori.core.domain.user

import java.util.UUID

object UserFixture {

    fun create(
        id: Long = 0L,
        publicId: String = UUID.randomUUID().toString(),
    ) = User(
        id = id,
        publicId = publicId,
    )

}
