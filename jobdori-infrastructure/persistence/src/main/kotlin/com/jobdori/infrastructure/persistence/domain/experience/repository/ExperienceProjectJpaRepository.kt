package com.jobdori.infrastructure.persistence.domain.experience.repository

import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.infrastructure.persistence.domain.experience.entity.ExperienceProjectEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ExperienceProjectJpaRepository : JpaRepository<ExperienceProjectEntity, Long> {

    fun findByIdAndWorkspaceIdAndStatus(
        id: Long,
        workspaceId: Long,
        status: ExperienceProjectStatus,
    ): ExperienceProjectEntity?

    fun findAllByIdInAndWorkspaceIdAndStatus(
        ids: Collection<Long>,
        workspaceId: Long,
        status: ExperienceProjectStatus,
    ): List<ExperienceProjectEntity>

    fun findAllByWorkspaceIdAndStatusOrderByIdDesc(
        workspaceId: Long,
        status: ExperienceProjectStatus,
        pageable: Pageable,
    ): List<ExperienceProjectEntity>

    fun findAllByWorkspaceIdAndStatusAndIdLessThanOrderByIdDesc(
        workspaceId: Long,
        status: ExperienceProjectStatus,
        id: Long,
        pageable: Pageable,
    ): List<ExperienceProjectEntity>

    fun countByWorkspaceIdAndStatus(
        workspaceId: Long,
        status: ExperienceProjectStatus,
    ): Long

}
