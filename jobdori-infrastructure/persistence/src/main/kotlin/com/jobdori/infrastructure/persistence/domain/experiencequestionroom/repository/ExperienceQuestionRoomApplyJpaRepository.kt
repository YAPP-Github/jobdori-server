package com.jobdori.infrastructure.persistence.domain.experiencequestionroom.repository

import com.jobdori.infrastructure.persistence.domain.experiencequestionroom.entity.ExperienceQuestionRoomApplyEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ExperienceQuestionRoomApplyJpaRepository : JpaRepository<ExperienceQuestionRoomApplyEntity, Long> {
    fun findByQuestionRoomIdAndResumeId(questionRoomId: String, resumeId: Long): ExperienceQuestionRoomApplyEntity?
}
