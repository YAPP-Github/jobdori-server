package com.jobdori.infrastructure.persistence.user.repository

import com.jobdori.core.domain.user.UserIdentityProvider

interface UserIdentityCustomRepository {

    fun existsByProviderAndProviderUserId(provider: UserIdentityProvider, providerUserId: String): Boolean

}
