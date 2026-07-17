package com.jobdori.infrastructure.persistence.domain.user.repository

import com.jobdori.core.domain.user.UserIdentity
import com.jobdori.core.domain.user.UserIdentityProvider
import com.jobdori.core.domain.user.repository.UserIdentityRepository
import com.jobdori.infrastructure.persistence.domain.user.entity.UserIdentityEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class UserIdentityRepositoryImpl(
    private val jpaRepository: UserIdentityJpaRepository,
) : UserIdentityRepository {

    @Transactional(readOnly = true)
    override fun existsByProviderAndProviderUserId(provider: UserIdentityProvider, providerUserId: String): Boolean {
        return jpaRepository.existsByProviderAndProviderUserId(provider = provider, providerUserId = providerUserId)
    }

    @Transactional(readOnly = true)
    override fun findByProviderAndProviderUserId(
        provider: UserIdentityProvider,
        providerUserId: String,
    ): UserIdentity? {
        val entity = jpaRepository.findByProviderAndProviderUserId(provider, providerUserId)
        return entity?.toDomain()
    }

    @Transactional
    override fun save(userIdentity: UserIdentity): UserIdentity {
        val entity = jpaRepository.save(UserIdentityEntity.from(userIdentity))
        return entity.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findAllByUserId(userId: Long): List<UserIdentity> {
        return jpaRepository.findAllByUserId(userId).map(UserIdentityEntity::toDomain)
    }

    @Transactional
    override fun deleteAllByUserId(userId: Long) {
        jpaRepository.deleteAllByUserId(userId)
    }

}
