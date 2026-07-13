package com.jobdori.core.domain.experiencerecommendation

import java.time.LocalDateTime

data class JdExperienceRecommendation(
    val id: Long,
    val jdId: Long,
    val items: List<RecommendedExperience>,
    // 생성 시점의 경험 세트 시그니처. 현재 시그니처와 다르면 경험이 바뀐 것 -> 재생성.
    val sourceSignature: String,
    val createdAt: LocalDateTime? = null,
) {

    companion object {
        fun newInstance(
            jdId: Long,
            items: List<RecommendedExperience>,
            sourceSignature: String,
        ) = JdExperienceRecommendation(
            id = 0L,
            jdId = jdId,
            items = items,
            sourceSignature = sourceSignature,
        )
    }

}
