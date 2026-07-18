package com.jobdori.api.application.resume.service

import com.jobdori.api.application.resume.dto.request.CreateResumeRequest
import com.jobdori.api.application.resume.dto.request.SaveResumeRequest
import com.jobdori.api.application.resume.dto.ResumeStatusType
import com.jobdori.api.application.resume.dto.ResumeOptimizationMode
import com.jobdori.api.application.resume.dto.response.ResumeResponse
import com.jobdori.api.application.resume.dto.response.ResumeStatusCountResponse
import com.jobdori.api.application.resume.dto.response.ResumeSummaryResponse
import com.jobdori.api.application.resume.dto.response.ResumeListResponse
import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.api.application.jd.dto.response.JdResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.core.application.jd.GetJdService
import com.jobdori.core.application.resume.ResumeExperiencePolishService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.resume.Resume
import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.ResumeExperiencePayload
import com.jobdori.core.domain.resume.service.ResumeCreator
import com.jobdori.core.domain.resume.service.ProfileResumeSectionInitializer
import com.jobdori.core.domain.resume.service.ResumeReader
import com.jobdori.core.domain.resume.service.ResumeRemover
import com.jobdori.core.domain.resume.service.ResumeModifier
import com.jobdori.core.domain.resume.service.command.ResumeSaveCommand
import com.jobdori.core.domain.profile.service.ProfileReader
import org.springframework.stereotype.Service

@Service
class ResumeService(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val resumeCreator: ResumeCreator,
    private val resumeReader: ResumeReader,
    private val resumeRemover: ResumeRemover,
    private val resumeModifier: ResumeModifier,
    private val getJdService: GetJdService,
    private val profileReader: ProfileReader,
    private val profileResumeSectionInitializer: ProfileResumeSectionInitializer,
    private val resumeExperiencePolishService: ResumeExperiencePolishService,
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
            )

            includeSections -> toResponse(
                workspaceId = workspace.id,
                detail = resumeReader.getSections(workspaceId = workspace.id, resumeId = resumeId),
                includeTargetJd = includeTargetJd,
            )

            else -> toResponse(
                workspaceId = workspace.id,
                resume = resumeReader.getResume(workspaceId = workspace.id, resumeId = resumeId),
                includeTargetJd = includeTargetJd,
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
        request: CreateResumeRequest,
        includeTargetJd: Boolean = false,
    ): ResumeResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        validatePolishTargetJd(request.optimizationMode, request.targetJdId)
        val targetJd = request.targetJdId?.let { publicId ->
            getJdService.getJd(workspaceId = workspace.id, publicId = publicId)
        }
        val command = request.toCommand(targetJd?.id).let { command ->
            if (request.sections.none { it.useDefaultItems }) command
            else {
                val profile = profileReader.getOrCreateProfile(workspace.id)
                val profileDetail = profileReader.getDetail(profile)
                command.copy(
                    sections = command.sections.mapIndexedNotNull { index, section ->
                        if (!request.sections[index].useDefaultItems) return@mapIndexedNotNull section

                        val defaultItems = profileResumeSectionInitializer.initializeItems(profileDetail, section.type)
                        section.copy(items = defaultItems).takeIf { defaultItems.isNotEmpty() }
                    },
                )
            }
        }.let { polishExperienceContents(it, request.optimizationMode, targetJd) }

        return toResponse(
            workspaceId = workspace.id,
            detail = resumeCreator.createDetail(
                workspaceId = workspace.id,
                command = command,
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

        validatePolishTargetJd(request.optimizationMode, request.targetJdId)
        val targetJd = request.targetJdId?.let { publicId ->
            getJdService.getJd(workspaceId = workspace.id, publicId = publicId)
        }
        val command = polishExperienceContents(request.toCommand(targetJd?.id), request.optimizationMode, targetJd)

        return toResponse(
            workspaceId = workspace.id,
            detail = resumeModifier.modifyDetail(
                workspaceId = workspace.id,
                resumeId = resumeId,
                command = command,
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

    private fun validatePolishTargetJd(optimizationMode: ResumeOptimizationMode, targetJdId: String?) {
        if (optimizationMode == ResumeOptimizationMode.JOB_SPECIFIC && targetJdId == null) {
            throw InvalidArgumentsException("첨삭 저장에는 대상 채용공고 ID가 필요합니다.")
        }
    }

    private fun polishExperienceContents(
        command: ResumeSaveCommand,
        optimizationMode: ResumeOptimizationMode,
        targetJd: Jd?,
    ): ResumeSaveCommand {
        if (optimizationMode != ResumeOptimizationMode.JOB_SPECIFIC || targetJd == null) return command

        return command.copy(
            sections = command.sections.map { section ->
                section.copy(
                    items = section.items.map { item ->
                        val payload = item.payload
                        val contents = (payload as? ResumeExperiencePayload)?.contents
                        if (payload !is ResumeExperiencePayload || contents.isNullOrBlank()) item
                        else item.copy(
                            payload = payload.copy(
                                contents = resumeExperiencePolishService.polish(contents, targetJd),
                            ),
                        )
                    },
                )
            },
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
    ): ResumeResponse {
        val jd = getTargetJd(workspaceId, resume, includeTargetJd)
        return ResumeResponse.from(
            resume = resume,
            targetJd = jd?.let(JdResponse::from),
        )
    }

    private fun toResponse(
        workspaceId: Long,
        detail: ResumeDetail,
        includeTargetJd: Boolean,
    ): ResumeResponse {
        val jd = getTargetJd(workspaceId, detail.resume, includeTargetJd)
        return ResumeResponse.from(
            detail = detail,
            targetJd = jd?.let(JdResponse::from),
        )
    }

    private fun getTargetJd(workspaceId: Long, resume: Resume, include: Boolean): Jd? =
        if (!include) null
        else resume.targetJdId?.let { targetJdId ->
            getJdService.getJd(workspaceId = workspaceId, id = targetJdId)
        }

    private fun getTargetJds(workspaceId: Long, resumes: List<Resume>): Map<Long, JdResponse> = getJdService.getJds(
        workspaceId = workspaceId,
        ids = resumes.mapNotNull { it.targetJdId },
    ).associate { jd -> jd.id to JdResponse.from(jd) }

}
