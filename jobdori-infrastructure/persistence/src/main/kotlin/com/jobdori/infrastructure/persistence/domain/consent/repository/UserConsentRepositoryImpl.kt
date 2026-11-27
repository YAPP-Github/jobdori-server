package com.jobdori.infrastructure.persistence.domain.consent.repository

import com.jobdori.core.domain.consent.UserConsent
import com.jobdori.core.domain.consent.repository.UserConsentRepository
import com.jobdori.infrastructure.persistence.domain.consent.entity.UserConsentEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class UserConsentRepositoryImpl(
    private val jpa: UserConsentJpaRepository,
) : UserConsentRepository {

    @Transactional(readOnly = true)
    override fun findAllByUserId(userId: Long): List<UserConsent> = jpa.findAllByUserId(userId).map { it.toDomain() }

    @Transactional
    override fun saveAll(consents: List<UserConsent>): List<UserConsent> =
        jpa.saveAll(consents.map(UserConsentEntity::from)).map { it.toDomain() }
}
