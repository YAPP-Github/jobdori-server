package com.jobdori.core.domain.user.service

import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.repository.UserIdentityRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserIdentityReader(
    private val userIdentityRepository: UserIdentityRepository,
) {

    @Transactional(readOnly = true)
    fun findIdentity(provider: UserIdentityProvider, providerUserId: String): UserIdentity? {
        return userIdentityRepository.findByProviderAndProviderUserId(
            provider = provider,
            providerUserId = providerUserId
        )
    }

}
