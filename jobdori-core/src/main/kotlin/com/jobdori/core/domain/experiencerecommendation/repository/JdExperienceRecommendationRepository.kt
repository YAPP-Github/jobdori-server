package com.jobdori.core.domain.experiencerecommendation.repository

import com.jobdori.core.domain.experiencerecommendation.JdExperienceRecommendation

interface JdExperienceRecommendationRepository {
    fun findByJdId(jdId: Long): JdExperienceRecommendation?

    // jdId 유니크 - 있으면 갱신, 없으면 삽입.
    fun upsert(recommendation: JdExperienceRecommendation): JdExperienceRecommendation

    fun deleteByJdId(jdId: Long)
}
