package com.jobdori.core.application.experiencequestionroom.result

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessage

data class ExperienceQuestionRoomSendResult(
    val message: ExperienceQuestionRoomMessage,
    val draftNumber: Int,
)
