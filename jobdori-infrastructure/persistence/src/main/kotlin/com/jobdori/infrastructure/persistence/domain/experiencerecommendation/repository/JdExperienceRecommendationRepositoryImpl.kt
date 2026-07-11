package com.jobdori.infrastructure.persistence.domain.experiencerecommendation.repository

import com.jobdori.core.domain.experiencerecommendation.JdExperienceRecommendation
import com.jobdori.core.domain.experiencerecommendation.repository.JdExperienceRecommendationRepository
import com.jobdori.infrastructure.persistence.domain.experiencerecommendation.entity.JdExperienceRecommendationEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class JdExperienceRecommendationRepositoryImpl(
    private val jpa: JdExperienceRecommendationJpaRepository,
) : JdExperienceRecommendationRepository {

    @Transactional(readOnly = true)
    override fun findByJdId(jdId: Long): JdExperienceRecommendation? =
        jpa.findByJdId(jdId)?.toDomain()

    @Transactional
    override fun upsert(recommendation: JdExperienceRecommendation): JdExperienceRecommendation {
        val entity = jpa.findByJdId(recommendation.jdId)?.apply {
            items = recommendation.items
            sourceSignature = recommendation.sourceSignature
        } ?: JdExperienceRecommendationEntity.from(recommendation)
        return jpa.save(entity).toDomain()
    }

}
