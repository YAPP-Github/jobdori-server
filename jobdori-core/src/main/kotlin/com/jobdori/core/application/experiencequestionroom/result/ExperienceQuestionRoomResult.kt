package com.jobdori.core.application.experiencequestionroom.result

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomSourceType

data class ExperienceQuestionRoomResult(
    val cards: List<Card> = emptyList(),
) {
    data class Card(
        val sourceType: ExperienceQuestionRoomSourceType = ExperienceQuestionRoomSourceType.RESPONSIBILITY,
        val index: Int = 0,
        val question: String = "",
    )
}
