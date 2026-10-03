package com.jobdori.api.application.experiencequestionroom.dto.response

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomCard
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomSourceType

data class ExperienceQuestionRoomCardResponse(
    val questionRoomId: String,
    val question: String,
    val sourceType: ExperienceQuestionRoomSourceType,
    val sourceText: String,
) {
    companion object {
        fun from(card: ExperienceQuestionRoomCard) = ExperienceQuestionRoomCardResponse(
            questionRoomId = card.id,
            question = card.question,
            sourceType = card.sourceType,
            sourceText = card.sourceText,
        )
    }
}
