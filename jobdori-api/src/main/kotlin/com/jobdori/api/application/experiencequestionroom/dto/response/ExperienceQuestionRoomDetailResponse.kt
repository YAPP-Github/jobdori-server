package com.jobdori.api.application.experiencequestionroom.dto.response

import com.jobdori.core.application.experiencequestionroom.result.ExperienceQuestionRoomDetail
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessageRole

data class ExperienceQuestionRoomDetailResponse(
    val room: ExperienceQuestionRoomCardResponse,
    val messages: List<ExperienceQuestionRoomMessageResponse>,
    val experiences: List<Experience>,
) {

    data class Experience(val experienceId: Long, val title: String)

    companion object {
        fun from(detail: ExperienceQuestionRoomDetail): ExperienceQuestionRoomDetailResponse {
            var draftNumber = 0
            return ExperienceQuestionRoomDetailResponse(
                room = ExperienceQuestionRoomCardResponse.from(detail.room),
                messages = detail.messages.map {
                    ExperienceQuestionRoomMessageResponse.from(
                        it,
                        if (it.role == ExperienceQuestionRoomMessageRole.AI) ++draftNumber else null,
                    )
                },
                experiences = detail.experiences.map { Experience(it.id, it.title) },
            )
        }
    }
}
