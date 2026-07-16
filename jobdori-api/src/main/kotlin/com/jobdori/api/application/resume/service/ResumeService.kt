package com.jobdori.api.application.resume.service

import com.jobdori.api.application.resume.dto.request.SaveResumeRequest
import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.api.application.resume.dto.response.ResumeResponse
import com.jobdori.api.application.resume.dto.response.ResumeStatusCountResponse
import com.jobdori.api.application.resume.dto.response.ResumeSummaryResponse
import com.jobdori.api.application.resume.dto.response.ResumeListResponse
import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.api.application.jd.dto.response.JdResponse
import com.jobdori.api.application.jd.dto.response.JdInsightResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.application.jdinsight.GetJdInsightService
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeDetail
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
    private val getJdService: GetJdService,
    private val getJdInsightService: GetJdInsightService,
) {

    fun getResumes(
        userId: Long,
        workspaceId: String,
        statuses: List<ResumeStatusType>?,
        cursor: String?,
        size: Int,
        includeTargetJd: Boolean = false,
    ): ResumeListResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val allowedStatuses = resolveListStatuses(statuses)

        val result = resumeReader.getResumes(
            workspaceId = workspace.id,
            statuses = allowedStatuses,
            cursor = cursor,
            size = size,
        )
        val resumes = result.items
        val targetJdsById = if (includeTargetJd) getTargetJds(workspace.id, resumes) else emptyMap()

        return ResumeListResponse(
            resumes = resumes.map { resume ->
            ResumeSummaryResponse.from(
                resume = resume,
                targetJd = resume.targetJdId?.let(targetJdsById::get),
            )
            },
            cursor = CursorResponse(nextCursor = result.nextCursor),
        )
    }

    fun getResume(
        userId: Long,
        workspaceId: String,
        resumeId: Long,
        includeSections: Boolean,
        includeSectionItems: Boolean,
        includeTargetJd: Boolean = false,
        includeJdInsight: Boolean = false,
    ): ResumeResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        return when {
            includeSectionItems -> toResponse(
                workspaceId = workspace.id,
                detail = resumeReader.getDetail(workspaceId = workspace.id, resumeId = resumeId),
                includeTargetJd = includeTargetJd,
                includeJdInsight = includeJdInsight,
            )

            includeSections -> toResponse(
                workspaceId = workspace.id,
                detail = resumeReader.getSections(workspaceId = workspace.id, resumeId = resumeId),
                includeTargetJd = includeTargetJd,
                includeJdInsight = includeJdInsight,
            )

            else -> toResponse(
                workspaceId = workspace.id,
                resume = resumeReader.getResume(workspaceId = workspace.id, resumeId = resumeId),
                includeTargetJd = includeTargetJd,
                includeJdInsight = includeJdInsight,
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
        includeTargetJd: Boolean = false,
    ): ResumeResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        return toResponse(
            workspaceId = workspace.id,
            detail = resumeCreator.createDetail(
                workspaceId = workspace.id,
                command = request.toCommand(),
            ),
            includeTargetJd = includeTargetJd,
        )
    }

    fun modifyResume(
        userId: Long,
        workspaceId: String,
        resumeId: Long,
        request: SaveResumeRequest,
        includeTargetJd: Boolean = false,
    ): ResumeResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        return toResponse(
            workspaceId = workspace.id,
            detail = resumeModifier.modifyDetail(
                workspaceId = workspace.id,
                resumeId = resumeId,
                command = request.toCommand(),
            ),
            includeTargetJd = includeTargetJd,
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

    private fun toResponse(
        workspaceId: Long,
        resume: Resume,
        includeTargetJd: Boolean,
        includeJdInsight: Boolean = false,
    ): ResumeResponse {
        val jd = getTargetJd(workspaceId, resume, includeTargetJd || includeJdInsight)
        return ResumeResponse.from(
            resume = resume,
            targetJd = jd?.takeIf { includeTargetJd }?.let(JdResponse::from),
            jdInsight = getJdInsight(workspaceId, jd, includeJdInsight),
        )
    }

    private fun toResponse(
        workspaceId: Long,
        detail: ResumeDetail,
        includeTargetJd: Boolean,
        includeJdInsight: Boolean = false,
    ): ResumeResponse {
        val jd = getTargetJd(workspaceId, detail.resume, includeTargetJd || includeJdInsight)
        return ResumeResponse.from(
            detail = detail,
            targetJd = jd?.takeIf { includeTargetJd }?.let(JdResponse::from),
            jdInsight = getJdInsight(workspaceId, jd, includeJdInsight),
        )
    }

    private fun getTargetJd(workspaceId: Long, resume: Resume, include: Boolean): Jd? =
        if (!include) null
        else resume.targetJdId?.let { targetJdId ->
            getJdService.getJd(workspaceId = workspaceId, id = targetJdId)
        }

    private fun getJdInsight(workspaceId: Long, jd: Jd?, include: Boolean): JdInsightResponse? =
        if (!include || jd == null) null
        else JdInsightResponse.from(
            getJdInsightService.getOrGenerate(workspaceId = workspaceId, jdPublicId = jd.publicId),
        )

    private fun getTargetJds(workspaceId: Long, resumes: List<Resume>): Map<Long, JdResponse> = getJdService.getJds(
        workspaceId = workspaceId,
        ids = resumes.mapNotNull { it.targetJdId },
    ).associate { jd -> jd.id to JdResponse.from(jd) }

}
