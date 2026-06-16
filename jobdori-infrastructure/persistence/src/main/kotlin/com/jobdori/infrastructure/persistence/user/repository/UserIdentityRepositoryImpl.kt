package com.jobdori.infrastructure.persistence.user.repository

import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.repository.UserIdentityRepository
import com.jobdori.infrastructure.persistence.user.entity.UserIdentityEntity
import org.springframework.stereotype.Repository

@Repository
class UserIdentityRepositoryImpl(
    private val jpaRepository: UserIdentityJpaRepository,
) : UserIdentityRepository {

    override fun existsByProviderAndProviderUserId(provider: UserIdentityProvider, providerUserId: String): Boolean {
        return jpaRepository.existsByProviderAndProviderUserId(provider = provider, providerUserId = providerUserId)
    }

    override fun findByProviderAndProviderUserId(
        provider: UserIdentityProvider,
        providerUserId: String,
    ): UserIdentity? {
        val entity = jpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
        return entity?.toDomain()
    }

    override fun save(userIdentity: UserIdentity): UserIdentity {
        val entity = jpaRepository.save(UserIdentityEntity.from(userIdentity))
        return entity.toDomain()
    }

}
