package com.jobdori.core.domain.user.repository

import com.jobdori.core.domain.user.UserIdentify
import com.jobdori.core.domain.user.UserIdentifyProvider

interface UserIdentifyRepository {

    fun findByProviderAndIdentifyId(
        provider: UserIdentifyProvider,
        identifyId: String,
    ): UserIdentify?

    fun save(userIdentify: UserIdentify): UserIdentify

}
