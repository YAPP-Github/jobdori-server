package com.jobdori.core.domain.user

data class UserIdentify(
    val id: Long,
    val userId: Long,
    val identifyProvider: UserIdentifyProvider,
    val identifyId: String,
) {

    companion object {
        fun newInstance(
            user: User,
            identifyProvider: UserIdentifyProvider,
            identifyId: String,
        ) = UserIdentify(
            id = 0L,
            userId = user.id,
            identifyProvider = identifyProvider,
            identifyId = identifyId,
        )
    }

}
