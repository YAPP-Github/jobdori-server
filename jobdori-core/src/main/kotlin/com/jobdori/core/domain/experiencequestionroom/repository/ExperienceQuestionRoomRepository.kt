package com.jobdori.core.domain.experiencequestionroom.repository

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoom

interface ExperienceQuestionRoomRepository {
    fun findByJdId(jdId: Long): ExperienceQuestionRoom?

    fun save(room: ExperienceQuestionRoom): ExperienceQuestionRoom
}
