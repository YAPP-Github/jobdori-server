package com.jobdori.infrastructure.persistence.domain.resume.repository

import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Pageable

interface ResumeJpaRepository : JpaRepository<ResumeEntity, Long>, ResumeCustomRepository {

    fun findByIdAndWorkspaceIdAndStatusIn(
        id: Long,
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
    ): ResumeEntity?

    fun findAllByWorkspaceIdAndStatusInOrderByIdDesc(
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
        pageable: Pageable,
    ): List<ResumeEntity>

    fun findAllByWorkspaceIdAndStatusInAndIdLessThanOrderByIdDesc(
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
        id: Long,
        pageable: Pageable,
    ): List<ResumeEntity>

}
