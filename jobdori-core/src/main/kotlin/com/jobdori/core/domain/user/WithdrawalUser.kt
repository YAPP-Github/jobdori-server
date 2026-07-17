package com.jobdori.core.domain.user

data class WithdrawalUser(
    val id: Long = 0L,
    val originalUserId: Long,
    val publicId: String,
) {

    companion object {
        fun from(user: User) = WithdrawalUser(
            originalUserId = user.id,
            publicId = user.publicId,
        )
    }

}
