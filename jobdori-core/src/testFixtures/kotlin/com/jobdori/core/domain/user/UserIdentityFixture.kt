package com.jobdori.core.domain.user

object UserIdentityFixture {

    fun create(
        id: Long = 0L,
        userId: Long = 0L,
        provider: UserIdentityProvider = UserIdentityProvider.GOOGLE,
        providerUserId: String = "google-identity-id",
    ) = UserIdentity(
        id = id,
        userId = userId,
        provider = provider,
        providerUserId = providerUserId,
    )

}
