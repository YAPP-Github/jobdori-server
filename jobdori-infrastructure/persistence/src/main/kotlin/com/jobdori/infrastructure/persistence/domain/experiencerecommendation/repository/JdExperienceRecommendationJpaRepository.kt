package com.jobdori.infrastructure.persistence.domain.experiencerecommendation.repository

import com.jobdori.infrastructure.persistence.domain.experiencerecommendation.entity.JdExperienceRecommendationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface JdExperienceRecommendationJpaRepository : JpaRepository<JdExperienceRecommendationEntity, Long> {
    fun findByJdId(jdId: Long): JdExperienceRecommendationEntity?
}
