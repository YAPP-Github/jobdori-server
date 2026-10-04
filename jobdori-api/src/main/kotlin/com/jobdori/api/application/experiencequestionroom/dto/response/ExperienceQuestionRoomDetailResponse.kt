package com.jobdori.api.application.experiencequestionroom.dto.response

import com.jobdori.core.application.experiencequestionroom.result.ExperienceQuestionRoomDetail

data class ExperienceQuestionRoomDetailResponse(
    val room: ExperienceQuestionRoomCardResponse,
    val messages: List<ExperienceQuestionRoomMessageResponse>,
) {

    companion object {
        fun from(detail: ExperienceQuestionRoomDetail) = ExperienceQuestionRoomDetailResponse(
            room = ExperienceQuestionRoomCardResponse.from(detail.room),
            messages = detail.messages.map(ExperienceQuestionRoomMessageResponse::from),
        )
    }
}
