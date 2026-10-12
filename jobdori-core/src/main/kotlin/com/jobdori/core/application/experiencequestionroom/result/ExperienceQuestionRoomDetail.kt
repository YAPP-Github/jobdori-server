package com.jobdori.core.application.experiencequestionroom.result

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomCard
import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessage

data class ExperienceQuestionRoomDetail(
    val room: ExperienceQuestionRoomCard,
    val messages: List<ExperienceQuestionRoomMessage>,
    val experiences: List<Experience> = emptyList(),
)
