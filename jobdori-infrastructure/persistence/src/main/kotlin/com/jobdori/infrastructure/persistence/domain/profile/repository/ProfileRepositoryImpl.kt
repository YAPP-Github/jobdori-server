package com.jobdori.infrastructure.persistence.domain.profile.repository

import com.jobdori.core.domain.profile.Profile
import com.jobdori.core.domain.profile.repository.ProfileRepository
import com.jobdori.core.support.crypto.StringEncryptor
import com.jobdori.infrastructure.persistence.domain.profile.entity.ProfileEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ProfileRepositoryImpl(
    private val jpaRepository: ProfileJpaRepository,
    private val encryptor: StringEncryptor,
) : ProfileRepository {

    @Transactional
    override fun save(profile: Profile): Profile {
        return jpaRepository.save(ProfileEntity.from(profile, encryptor)).toDomain(encryptor)
    }

    @Transactional(readOnly = true)
    override fun findByWorkspaceId(workspaceId: Long): Profile? {
        return jpaRepository.findByWorkspaceId(workspaceId)?.toDomain(encryptor)
    }

}
