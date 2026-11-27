package com.jobdori.infrastructure.persistence.domain.consent.repository

import com.jobdori.infrastructure.persistence.domain.consent.entity.UserConsentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserConsentJpaRepository : JpaRepository<UserConsentEntity, Long> {
    fun findAllByUserId(userId: Long): List<UserConsentEntity>
}
