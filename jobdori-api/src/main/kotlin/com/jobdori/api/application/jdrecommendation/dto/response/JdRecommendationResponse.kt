package com.jobdori.api.application.jdrecommendation.dto.response

import com.jobdori.core.domain.jdrecommendation.JdRecommendation
import com.jobdori.core.domain.jdrecommendation.JdRecommendationTag

data class JdRecommendationResponse(
    val id: Long,
    val tags: List<JdRecommendationTag>?,
    val companyName: String,
    val title: String,
    val recruitmentStartAt: String?,
    val recruitmentEndAt: String?,
    val sourceUrl: String,
    val displayOrder: Double,
) {
    companion object {
        fun from(recommendation: JdRecommendation) = JdRecommendationResponse(
            id = recommendation.id,
            tags = recommendation.tags,
            companyName = recommendation.companyName,
            title = recommendation.title,
            recruitmentStartAt = recommendation.recruitmentStartAt?.toString(),
            recruitmentEndAt = recommendation.recruitmentEndAt?.toString(),
            sourceUrl = recommendation.sourceUrl,
            displayOrder = recommendation.displayOrder,
        )
    }
}
