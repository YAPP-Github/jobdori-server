package com.jobdori.infrastructure.persistence.domain.experiencequestionroom.repository

import com.jobdori.infrastructure.persistence.domain.experiencequestionroom.entity.ExperienceQuestionRoomMessageEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ExperienceQuestionRoomMessageJpaRepository : JpaRepository<ExperienceQuestionRoomMessageEntity, Long> {
    fun findAllByQuestionRoomIdOrderByCreatedAtAscIdAsc(questionRoomId: String): List<ExperienceQuestionRoomMessageEntity>
}
