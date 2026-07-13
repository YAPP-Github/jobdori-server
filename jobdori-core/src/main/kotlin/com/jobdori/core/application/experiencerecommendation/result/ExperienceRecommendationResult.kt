package com.jobdori.core.application.experiencerecommendation.result

/** 필드가 data.sql EXPERIENCE_RECOMMENDATION jsonSchema와 1:1 대응한다. */
data class ExperienceRecommendationResult(
    val scores: List<Score> = emptyList(),
    val reasons: List<Reason> = emptyList(),
) {
    data class Score(
        val index: Int = 0,
        val matchRate: Int = 0,
    )

    data class Reason(
        val index: Int = 0,
        val reason: String = "",
    )
}
