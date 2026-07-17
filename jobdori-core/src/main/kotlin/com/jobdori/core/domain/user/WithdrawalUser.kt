package com.jobdori.core.domain.user

data class WithdrawalUser(
    val id: Long = 0L,
    val originalUserId: Long,
    val publicId: String,
    val email: String,
    val name: String,
    val profileImageUrl: String?,
    val userIdentities: List<WithdrawalUserIdentity>,
) {

    companion object {
        fun from(user: User, identities: List<UserIdentity>) = WithdrawalUser(
            originalUserId = user.id,
            publicId = user.publicId,
            email = user.email,
            name = user.name,
            profileImageUrl = user.profileImageUrl,
            userIdentities = identities.map(WithdrawalUserIdentity::from),
        )
    }

}
