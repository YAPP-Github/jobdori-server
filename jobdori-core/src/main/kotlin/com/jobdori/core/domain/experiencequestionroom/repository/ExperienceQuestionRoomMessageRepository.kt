package com.jobdori.core.domain.experiencequestionroom.repository

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessage

interface ExperienceQuestionRoomMessageRepository {
    fun findAllByQuestionRoomId(questionRoomId: String): List<ExperienceQuestionRoomMessage>

    fun savePair(user: ExperienceQuestionRoomMessage, ai: ExperienceQuestionRoomMessage): List<ExperienceQuestionRoomMessage>
}
