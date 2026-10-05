package com.jobdori.core.application.experiencequestionroom.result

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessage

data class ExperienceQuestionRoomDetail(
    val room: ExperienceQuestionRoomCardResult,
    val messages: List<ExperienceQuestionRoomMessage>,
)
