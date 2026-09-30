package com.jobdori.api.application.jdrecommendation.dto.response

import com.jobdori.core.domain.jdrecommendation.JdRecommendationTag

data class JdRecommendationTagResponse(
    val tag: JdRecommendationTag,
    val name: String,
) {
    companion object {
        fun from(tag: JdRecommendationTag) = JdRecommendationTagResponse(
            tag = tag,
            name = tag.displayName,
        )
    }
}
