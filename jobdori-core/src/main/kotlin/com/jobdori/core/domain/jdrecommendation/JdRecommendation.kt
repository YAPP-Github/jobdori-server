package com.jobdori.core.domain.jdrecommendation

import java.time.LocalDateTime

data class JdRecommendation(
    val id: Long,
    val tags: List<JdRecommendationTag>?,
    val companyName: String,
    val title: String,
    val recruitmentStartAt: LocalDateTime?,
    val recruitmentEndAt: LocalDateTime?,
    val sourceUrl: String,
    val displayOrder: Double,
    val isActive: Boolean,
    val createdAt: LocalDateTime?,
)
