package com.jobdori.infrastructure.persistence.domain.resume.repository

import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeSectionEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ResumeSectionJpaRepository : JpaRepository<ResumeSectionEntity, Long> {

    fun findAllByResumeIdOrderByDisplayOrderAscIdAsc(
        resumeId: Long,
    ): List<ResumeSectionEntity>

    fun findAllByResumeId(
        resumeId: Long,
    ): List<ResumeSectionEntity>

}
