package com.jobdori.core.domain.experiencerecommendation

data class RecommendedExperience(
    val experienceId: Long,
    val matchRate: Int,
    // 상위 5개에만 채워지고 나머지는 null.
    val reason: String? = null,
)
