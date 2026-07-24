package com.jobdori.core.domain.resume.repository

import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeStatus
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand

interface ResumeRepository {

    fun save(resume: Resume): Resume

    fun findAllByWorkspaceIdAndStatuses(
        workspaceId: Long,
        statuses: Collection<ResumeStatus>,
        cursorId: Long?,
        size: Int,
    ): List<Resume>

    fun countByWorkspaceIdAndStatuses(workspaceId: Long, statuses: Collection<ResumeStatus>): Map<ResumeStatus, Long>

    fun findByIdAndWorkspaceId(id: Long, workspaceId: Long): Resume?

    fun markCoreCompetencyGenerated(id: Long, workspaceId: Long): Boolean

    fun resetCoreCompetencyGenerated(id: Long, workspaceId: Long)

    fun findSectionsByIdAndWorkspaceId(id: Long, workspaceId: Long): ResumeDetail?

    fun findDetailByIdAndWorkspaceId(id: Long, workspaceId: Long): ResumeDetail?

    fun createDetail(workspaceId: Long, command: ResumeSaveCommand): ResumeDetail

    fun modifyDetail(id: Long, workspaceId: Long, command: ResumeSaveCommand): ResumeDetail?

}
