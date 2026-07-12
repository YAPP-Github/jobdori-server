package com.jobdori.infrastructure.persistence.domain.resume.repository

import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ResumeJpaRepository : JpaRepository<ResumeEntity, Long>, ResumeCustomRepository {

    fun findByIdAndWorkspaceIdAndStatusIn(
        id: Long,
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
    ): ResumeEntity?

    fun findAllByWorkspaceIdAndStatusInOrderByUpdatedAtDescIdDesc(
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
    ): List<ResumeEntity>

}
