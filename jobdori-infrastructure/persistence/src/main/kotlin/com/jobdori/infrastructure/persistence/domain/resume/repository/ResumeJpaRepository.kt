package com.jobdori.infrastructure.persistence.domain.resume.repository

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
        set resume.coreCompetencyGenerated = true
        where resume.id = :id
          and resume.workspaceId = :workspaceId
          and resume.status in :statuses
          and resume.coreCompetencyGenerated = false
        """,
    )
    fun markCoreCompetencyGenerated(
        @Param("id") id: Long,
        @Param("workspaceId") workspaceId: Long,
        @Param("statuses") statuses: Collection<ResumeStatus>,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        update ResumeEntity resume
        set resume.coreCompetencyGenerated = false
        where resume.id = :id
          and resume.workspaceId = :workspaceId
          and resume.coreCompetencyGenerated = true
        """,
    )
    fun resetCoreCompetencyGenerated(
        @Param("id") id: Long,
        @Param("workspaceId") workspaceId: Long,
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
