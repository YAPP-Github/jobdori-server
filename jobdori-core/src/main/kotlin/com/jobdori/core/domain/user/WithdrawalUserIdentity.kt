package com.jobdori.core.domain.user

data class WithdrawalUserIdentity(
    val provider: UserIdentityProvider,
    val providerUserId: String,
) {

    companion object {
        fun from(identity: UserIdentity) = WithdrawalUserIdentity(
            provider = identity.provider,
            providerUserId = identity.providerUserId,
        )
    }

}
