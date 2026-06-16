package com.jobdori.core.domain.user

object UserIdentifyFixture {

    fun create(
        id: Long = 0L,
        userId: Long = 0L,
        identifyProvider: UserIdentifyProvider = UserIdentifyProvider.GOOGLE,
        identifyId: String = "google-identify-id",
    ) = UserIdentify(
        id = id,
        userId = userId,
        identifyProvider = identifyProvider,
        identifyId = identifyId,
    )

}
