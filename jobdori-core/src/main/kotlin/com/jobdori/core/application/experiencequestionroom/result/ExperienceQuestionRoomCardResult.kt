package com.jobdori.core.application.experiencequestionroom.result

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomCard

data class ExperienceQuestionRoomCardResult(
    val card: ExperienceQuestionRoomCard,
    val applied: Boolean,
)
