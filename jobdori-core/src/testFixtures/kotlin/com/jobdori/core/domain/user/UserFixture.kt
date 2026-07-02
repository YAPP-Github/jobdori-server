package com.jobdori.core.domain.user

import java.util.UUID

object UserFixture {

    fun create(
        id: Long = 0L,
        publicId: String = UUID.randomUUID().toString(),
        email: String = "hong@example.com",
        name: String = "홍길동",
        profileImageUrl: String? = "https://jobdori.com/profile.png",
    ) = User(
        id = id,
        publicId = publicId,
        email = email,
        name = name,
        profileImageUrl = profileImageUrl,
    )

}
