package com.jobdori.core.domain.user

data class UserIdentity(
    val id: Long,
    val userId: Long,
    val provider: UserIdentityProvider,
    val providerUserId: String,
) {

    companion object {
        fun newInstance(
            user: User,
            provider: UserIdentityProvider,
            providerUserId: String,
        ) = UserIdentity(
            id = 0L,
            userId = user.id,
            provider = provider,
            providerUserId = providerUserId,
        )
    }

}
