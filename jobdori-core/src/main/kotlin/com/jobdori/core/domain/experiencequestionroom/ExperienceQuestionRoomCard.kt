package com.jobdori.core.domain.experiencequestionroom

data class ExperienceQuestionRoomCard(
    val id: String,
    val question: String,
    val sourceType: ExperienceQuestionRoomSourceType,
    val sourceText: String,
)
