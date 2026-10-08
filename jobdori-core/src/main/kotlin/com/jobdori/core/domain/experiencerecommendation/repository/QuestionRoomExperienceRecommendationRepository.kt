package com.jobdori.core.domain.experiencerecommendation.repository

import com.jobdori.core.domain.experiencerecommendation.QuestionRoomExperienceRecommendation

interface QuestionRoomExperienceRecommendationRepository {
    fun findByQuestionRoomId(questionRoomId: String): QuestionRoomExperienceRecommendation?

    fun upsert(recommendation: QuestionRoomExperienceRecommendation): QuestionRoomExperienceRecommendation
}
