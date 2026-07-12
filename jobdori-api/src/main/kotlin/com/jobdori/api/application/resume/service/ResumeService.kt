package com.jobdori.api.application.resume.service

import com.jobdori.api.application.resume.dto.request.SaveResumeRequest
import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.api.application.resume.dto.response.ResumeResponse
import com.jobdori.api.application.resume.dto.response.ResumeStatusCountResponse
import com.jobdori.api.application.resume.dto.response.ResumeSummaryResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.core.domain.resume.service.ResumeCreator
import com.jobdori.core.domain.resume.service.ResumeReader
import com.jobdori.core.domain.resume.service.ResumeRemover
import com.jobdori.core.domain.resume.service.ResumeModifier
import org.springframework.stereotype.Service

@Service
class ResumeService(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val resumeCreator: ResumeCreator,
    private val resumeReader: ResumeReader,
    private val resumeRemover: ResumeRemover,
    private val resumeModifier: ResumeModifier,
) {

    fun getResumes(
        userId: Long,
        workspaceId: String,
        statuses: List<ResumeStatusType>?,
    ): List<ResumeSummaryResponse> {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val allowedStatuses = resolveListStatuses(statuses)

        return resumeReader.getResumes(
            workspaceId = workspace.id,
            statuses = allowedStatuses,
        ).map { ResumeSummaryResponse.from(it) }
    }

    fun getResume(
        userId: Long,
        workspaceId: String,
        resumeId: Long,
        includeSections: Boolean,
        includeSectionItems: Boolean,
    ): ResumeResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        return when {
            includeSectionItems -> ResumeResponse.from(
                resumeReader.getDetail(workspaceId = workspace.id, resumeId = resumeId),
            )

            includeSections -> ResumeResponse.from(
                resumeReader.getSections(workspaceId = workspace.id, resumeId = resumeId),
            )

            else -> ResumeResponse.from(
                resumeReader.getResume(workspaceId = workspace.id, resumeId = resumeId),
            )
        }
    }

    fun countResumes(
        userId: Long,
        workspaceId: String,
    ): List<ResumeStatusCountResponse> {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val statuses = listOf(ResumeStatusType.COMPLETED, ResumeStatusType.DRAFT)
        val counts = resumeReader.countResumes(
            workspaceId = workspace.id,
            statuses = statuses.map { it.toDomain() },
        )

        return statuses.map { status ->
            ResumeStatusCountResponse(
                status = status,
                count = counts[status.toDomain()] ?: 0L,
            )
        }
    }

    fun createResume(
        userId: Long,
        workspaceId: String,
        request: SaveResumeRequest,
    ): ResumeResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        return ResumeResponse.from(
            resumeCreator.createDetail(
                workspaceId = workspace.id,
                command = request.toCommand(),
            ),
        )
    }

    fun modifyResume(
        userId: Long,
        workspaceId: String,
        resumeId: Long,
        request: SaveResumeRequest,
    ): ResumeResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        return ResumeResponse.from(
            resumeModifier.modifyDetail(
                workspaceId = workspace.id,
                resumeId = resumeId,
                command = request.toCommand(),
            ),
        )
    }

    fun deleteResume(userId: Long, workspaceId: String, resumeId: Long) {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        resumeRemover.remove(
            workspaceId = workspace.id,
            resumeId = resumeId,
        )
    }

    private fun resolveListStatuses(statuses: List<ResumeStatusType>?) = (
        statuses?.takeIf { it.isNotEmpty() } ?: listOf(ResumeStatusType.COMPLETED, ResumeStatusType.DRAFT)
        )
        .distinct()
        .map { it.toDomain() }

}
