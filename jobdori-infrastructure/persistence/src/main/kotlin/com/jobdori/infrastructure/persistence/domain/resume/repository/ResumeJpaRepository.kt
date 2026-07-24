package com.jobdori.infrastructure.persistence.domain.resume.repository

import com.jobdori.core.domain.resume.CoreCompetencyGenerationStatus
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.infrastructure.persistence.domain.resume.entity.ResumeEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ResumeJpaRepository : JpaRepository<ResumeEntity, Long>, ResumeCustomRepository {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update ResumeEntity resume
        set resume.coreCompetencyGenerationStatus = :nextStatus
        where resume.id = :id
          and resume.workspaceId = :workspaceId
          and resume.status in :statuses
          and resume.coreCompetencyGenerationStatus = :currentStatus
        """,
    )
    fun updateCoreCompetencyGenerationStatus(
        @Param("id") id: Long,
        @Param("workspaceId") workspaceId: Long,
        @Param("statuses") statuses: Collection<ResumeStatus>,
        @Param("currentStatus") currentStatus: CoreCompetencyGenerationStatus,
        @Param("nextStatus") nextStatus: CoreCompetencyGenerationStatus,
    ): Int

    fun findByIdAndWorkspaceIdAndStatusIn(
        id: Long,
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
    ): ResumeEntity?

    fun existsByIdAndWorkspaceIdAndStatusIn(
        id: Long,
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
    ): Boolean

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
