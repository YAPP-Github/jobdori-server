package com.jobdori.infrastructure.persistence.domain.consent.repository

import com.jobdori.infrastructure.persistence.domain.consent.entity.ConsentSheetExposureEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ConsentSheetExposureJpaRepository : JpaRepository<ConsentSheetExposureEntity, Long>
