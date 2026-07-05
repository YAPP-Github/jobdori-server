package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.repository.UserIdentityRepository
import org.springframework.stereotype.Service

@Service
class UserIdentityReader(
    private val userIdentityRepository: UserIdentityRepository,
) {

    fun findIdentity(provider: UserIdentityProvider, providerUserId: String): UserIdentity? {
        return userIdentityRepository.findByProviderAndProviderUserId(
            provider = provider,
            providerUserId = providerUserId
        )
    }

}
