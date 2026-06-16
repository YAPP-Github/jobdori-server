package com.jobdori.infrastructure.persistence.user.repository

import com.jobdori.core.domain.user.UserIdentify
import com.jobdori.core.domain.user.UserIdentifyProvider
import com.jobdori.core.domain.user.repository.UserIdentifyRepository
import com.jobdori.infrastructure.persistence.user.entity.UserIdentifyEntity
import org.springframework.stereotype.Repository

@Repository
class UserIdentifyRepositoryImpl(
    private val jpaRepository: UserIdentifyJpaRepository,
) : UserIdentifyRepository {

    override fun findByProviderAndIdentifyId(
        provider: UserIdentifyProvider,
        identifyId: String,
    ): UserIdentify? {
        return jpaRepository.findByIdentifyProviderAndIdentifyId(provider, identifyId)?.toDomain()
    }

    override fun save(userIdentify: UserIdentify): UserIdentify {
        val entity = jpaRepository.save(UserIdentifyEntity.from(userIdentify))
        return entity.toDomain()
    }

}
