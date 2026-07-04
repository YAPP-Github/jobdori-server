package com.jobdori.infrastructure.persistence.domain.experience.repository

import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ExperienceJpaRepository : JpaRepository<ExperienceEntity, Long>, ExperienceCustomRepository {

    fun findByIdAndWorkspaceIdAndStatus(
        id: Long,
        workspaceId: Long,
        status: ExperienceStatus,
    ): ExperienceEntity?

    fun findAllByWorkspaceIdAndStatusOrderByIdDesc(
        workspaceId: Long,
        status: ExperienceStatus,
        pageable: Pageable,
    ): List<ExperienceEntity>

    fun findAllByWorkspaceIdAndStatusAndIdLessThanOrderByIdDesc(
        workspaceId: Long,
        status: ExperienceStatus,
        id: Long,
        pageable: Pageable,
    ): List<ExperienceEntity>

    fun findAllByWorkspaceIdAndProjectIdAndStatusOrderByIdDesc(
        workspaceId: Long,
        projectId: Long,
        status: ExperienceStatus,
        pageable: Pageable,
    ): List<ExperienceEntity>

    fun findAllByWorkspaceIdAndProjectIdAndStatusAndIdLessThanOrderByIdDesc(
        workspaceId: Long,
        projectId: Long,
        status: ExperienceStatus,
        id: Long,
        pageable: Pageable,
    ): List<ExperienceEntity>

}
