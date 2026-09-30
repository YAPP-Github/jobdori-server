package com.jobdori.core.domain.jdrecommendation.repository

import com.jobdori.core.domain.jdrecommendation.JdRecommendation
import com.jobdori.core.domain.jdrecommendation.JdRecommendationTag

interface JdRecommendationRepository {
    fun findAllActive(cursorDisplayOrder: Double?, cursorId: Long?, size: Int): List<JdRecommendation>

    fun findAllActiveContainingTags(
        tags: Collection<JdRecommendationTag>,
        cursorDisplayOrder: Double?,
        cursorId: Long?,
        size: Int,
    ): List<JdRecommendation>
}
