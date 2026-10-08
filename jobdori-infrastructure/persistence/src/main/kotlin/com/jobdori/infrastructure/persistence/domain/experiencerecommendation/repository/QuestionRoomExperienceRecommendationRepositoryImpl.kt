package com.jobdori.infrastructure.persistence.domain.experiencerecommendation.repository

import com.jobdori.core.domain.experiencerecommendation.QuestionRoomExperienceRecommendation
import com.jobdori.core.domain.experiencerecommendation.repository.QuestionRoomExperienceRecommendationRepository
import com.jobdori.infrastructure.persistence.domain.experiencerecommendation.entity.QuestionRoomExperienceRecommendationEntity
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class QuestionRoomExperienceRecommendationRepositoryImpl(
    private val jpa: QuestionRoomExperienceRecommendationJpaRepository,
) : QuestionRoomExperienceRecommendationRepository {

    @Transactional(readOnly = true)
    override fun findByQuestionRoomId(questionRoomId: String): QuestionRoomExperienceRecommendation? =
        jpa.findByQuestionRoomId(questionRoomId)?.toDomain()

    @Transactional
    override fun upsert(recommendation: QuestionRoomExperienceRecommendation): QuestionRoomExperienceRecommendation {
        val entity = jpa.findByQuestionRoomId(recommendation.questionRoomId)?.apply {
            items = recommendation.items
            sourceSignature = recommendation.sourceSignature
        } ?: QuestionRoomExperienceRecommendationEntity.from(recommendation)
        return jpa.save(entity).toDomain()
    }

}
