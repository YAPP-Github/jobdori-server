package com.jobdori.core.domain.user.repository

import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider

interface UserIdentityRepository {

    fun existsByProviderAndProviderUserId(
        provider: UserIdentityProvider,
        providerUserId: String,
    ): Boolean

    fun findByProviderAndProviderUserId(
        provider: UserIdentityProvider,
        providerUserId: String,
    ): UserIdentity?

    fun save(userIdentity: UserIdentity): UserIdentity

}
