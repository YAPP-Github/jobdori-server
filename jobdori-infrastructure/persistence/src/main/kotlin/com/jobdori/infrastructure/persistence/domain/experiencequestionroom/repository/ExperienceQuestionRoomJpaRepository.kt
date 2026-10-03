package com.jobdori.infrastructure.persistence.domain.experiencequestionroom.repository

import com.jobdori.infrastructure.persistence.domain.experiencequestionroom.entity.ExperienceQuestionRoomEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ExperienceQuestionRoomJpaRepository : JpaRepository<ExperienceQuestionRoomEntity, Long> {
    fun findByJdId(jdId: Long): ExperienceQuestionRoomEntity?
    fun findAllByJdIdIn(jdIds: Collection<Long>): List<ExperienceQuestionRoomEntity>
}
