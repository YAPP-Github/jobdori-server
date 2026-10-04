package com.jobdori.api.application.experiencequestionroom.dto.response

import com.jobdori.core.application.experiencequestionroom.result.ExperienceQuestionRoomCardResult
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomSourceType

data class ExperienceQuestionRoomCardResponse(
    val questionRoomId: String,
    val question: String,
    val sourceType: ExperienceQuestionRoomSourceType,
    val sourceText: String,
    val applied: Boolean,
) {
    companion object {
        fun from(result: ExperienceQuestionRoomCardResult) = ExperienceQuestionRoomCardResponse(
            questionRoomId = result.card.id,
            question = result.card.question,
            sourceType = result.card.sourceType,
            sourceText = result.card.sourceText,
            applied = result.applied,
        )
    }
}
