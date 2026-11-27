package com.jobdori.infrastructure.persistence.domain.consent.repository

import com.jobdori.core.domain.consent.repository.ConsentSheetExposureRepository
import com.jobdori.infrastructure.persistence.domain.consent.entity.ConsentSheetExposureEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ConsentSheetExposureRepositoryImpl(
    private val jpa: ConsentSheetExposureJpaRepository,
) : ConsentSheetExposureRepository {

    @Transactional(readOnly = true)
    override fun existsByUserId(userId: Long): Boolean = jpa.existsById(userId)

    @Transactional
    override fun save(userId: Long) {
        jpa.save(ConsentSheetExposureEntity(userId))
    }
}
