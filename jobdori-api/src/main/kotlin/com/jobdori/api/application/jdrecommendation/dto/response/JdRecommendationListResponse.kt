package com.jobdori.api.application.jdrecommendation.dto.response

import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.common.model.SliceResult
import com.jobdori.core.domain.jdrecommendation.JdRecommendation

data class JdRecommendationListResponse(
    val recommendations: List<JdRecommendationResponse>,
    val cursor: CursorResponse,
) {
    companion object {
        fun from(result: SliceResult<JdRecommendation>) = JdRecommendationListResponse(
            recommendations = result.items.map(JdRecommendationResponse::from),
            cursor = CursorResponse(nextCursor = result.nextCursor),
        )
    }
}
