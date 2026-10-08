package com.jobdori.infrastructure.persistence.domain.experiencerecommendation.repository

import com.jobdori.infrastructure.persistence.domain.experiencerecommendation.entity.QuestionRoomExperienceRecommendationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface QuestionRoomExperienceRecommendationJpaRepository : JpaRepository<QuestionRoomExperienceRecommendationEntity, Long> {
    fun findByQuestionRoomId(questionRoomId: String): QuestionRoomExperienceRecommendationEntity?
}
