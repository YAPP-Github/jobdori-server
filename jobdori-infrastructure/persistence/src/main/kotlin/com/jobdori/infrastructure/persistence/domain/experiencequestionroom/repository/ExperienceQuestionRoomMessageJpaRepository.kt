package com.jobdori.infrastructure.persistence.domain.experiencequestionroom.repository

import com.jobdori.core.domain.experiencequestionroom.ExperienceQuestionRoomMessageRole
import com.jobdori.infrastructure.persistence.domain.experiencequestionroom.entity.ExperienceQuestionRoomMessageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ExperienceQuestionRoomMessageJpaRepository : JpaRepository<ExperienceQuestionRoomMessageEntity, Long> {
    fun findAllByQuestionRoomIdOrderByCreatedAtAscIdAsc(questionRoomId: String): List<ExperienceQuestionRoomMessageEntity>

    fun findByQuestionRoomIdAndIdAndRole(
        questionRoomId: String,
        id: Long,
        role: ExperienceQuestionRoomMessageRole,
    ): ExperienceQuestionRoomMessageEntity?
}
