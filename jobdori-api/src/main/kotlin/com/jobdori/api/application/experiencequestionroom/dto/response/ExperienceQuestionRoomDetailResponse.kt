package com.jobdori.api.application.experiencequestionroom.dto.response

import com.jobdori.core.application.experiencequestionroom.result.ExperienceQuestionRoomDetail
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessageRole

data class ExperienceQuestionRoomDetailResponse(
    val room: ExperienceQuestionRoomCardResponse,
    val messages: List<ExperienceQuestionRoomMessageResponse>,
) {

    companion object {
        fun from(detail: ExperienceQuestionRoomDetail): ExperienceQuestionRoomDetailResponse {
            var draftNumber = 0
            val titleById = detail.experiences.associate { it.id to it.title }
            return ExperienceQuestionRoomDetailResponse(
                room = ExperienceQuestionRoomCardResponse.from(detail.room),
                messages = detail.messages.map {
                    ExperienceQuestionRoomMessageResponse.from(
                        it,
                        if (it.role == ExperienceQuestionRoomMessageRole.AI) ++draftNumber else null,
                        titleById,
                    )
                },
            )
        }
    }
}
