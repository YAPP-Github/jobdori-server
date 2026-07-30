package com.jobdori.api.application.experience.service

import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.api.application.experience.dto.request.CreateExperienceRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceRequest
import com.jobdori.api.application.experience.dto.request.contents.ExperienceContentsRequest
import com.jobdori.api.application.experience.dto.response.ExperienceListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.logger.LoggerExtension.log
import com.jobdori.core.application.experience.ExperienceContentsPolishService
import com.jobdori.core.application.experiencerecommendation.GetExperienceRecommendationService
import com.jobdori.core.application.profile.FirstExperienceCoreCompetencyService
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceContentsType
import com.jobdori.core.domain.experience.service.ExperienceCreator
import com.jobdori.core.domain.experience.service.ExperienceModifier
import com.jobdori.core.domain.experience.service.ExperienceProjectReader
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experience.service.ExperienceRemover
import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import org.springframework.stereotype.Service

@Service
class ExperienceService(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val experienceCreator: ExperienceCreator,
    private val experienceReader: ExperienceReader,
    private val experienceModifier: ExperienceModifier,
    private val experienceRemover: ExperienceRemover,
    private val experienceProjectReader: ExperienceProjectReader,
    private val getExperienceRecommendationService: GetExperienceRecommendationService,
    private val experienceContentsPolishService: ExperienceContentsPolishService,
    private val firstExperienceCoreCompetencyService: FirstExperienceCoreCompetencyService,
) {

    fun createExperience(
        userId: Long,
        workspaceId: String,
        projectId: Long,
        request: CreateExperienceRequest,
    ): ExperienceResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        val project = experienceProjectReader.getProject(workspaceId = workspace.id, projectId = projectId)
        // 경험 생성 전에 판정해야 한다. 생성 후에는 방금 만든 경험이 포함돼 항상 non-empty가 되어 절대 트리거되지 않는다.
        val isFirstExperience = experienceReader.findAllActive(workspace.id).isEmpty()
        val experience = experienceCreator.create(
            workspaceId = workspace.id,
            projectId = projectId,
            command = ExperienceCreateCommand(
                title = request.title,
                contents = resolveContents(request.contents),
                tags = request.tags,
                period = request.period?.toPeriod() ?: project.period,
                role = request.role ?: project.role,
            ),
        )

        if (isFirstExperience) {
            runCatching {
                firstExperienceCoreCompetencyService.generateIfAbsent(workspace.id, experience)
            }.onFailure { e ->
                log.warn(e) { "첫 경험 핵심역량 생성 실패, 경험 등록은 유지: workspaceId=${workspace.id}" }
            }
        }

        return ExperienceResponse.from(
            experience = experience,
            project = ExperienceProjectResponse.from(project),
        )
    }

    fun modifyExperience(
        userId: Long,
        workspaceId: String,
        experienceId: Long,
        request: UpdateExperienceRequest,
    ): ExperienceResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        experienceProjectReader.getProject(
            workspaceId = workspace.id,
            projectId = request.projectId,
        )

        val modified = experienceModifier.modify(
            workspaceId = workspace.id,
            experienceId = experienceId,
            projectId = request.projectId,
            tags = request.tags,
            title = request.title,
            contents = resolveContents(request.contents),
            period = request.period?.toPeriod(),
            role = request.role,
        )
        val project = experienceProjectReader.getProject(
            workspaceId = workspace.id,
            projectId = modified.projectId,
        )

        return ExperienceResponse.from(
            experience = modified,
            project = ExperienceProjectResponse.from(project),
        )
    }

    fun removeExperience(userId: Long, workspaceId: String, experienceId: Long) {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        experienceRemover.remove(
            workspaceId = workspace.id,
            experienceId = experienceId,
        )
    }

    fun getExperience(
        userId: Long,
        workspaceId: String,
        experienceId: Long,
        includeProject: Boolean,
    ): ExperienceResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        val experience = experienceReader.getExperience(
            workspaceId = workspace.id,
            experienceId = experienceId,
        )
        val project = if (includeProject) {
            ExperienceProjectResponse.from(
                experienceProjectReader.getProject(workspaceId = workspace.id, projectId = experience.projectId),
            )
        } else {
            null
        }

        return ExperienceResponse.from(
            experience = experience,
            project = project,
        )
    }

    fun getExperiences(
        userId: Long,
        workspaceId: String,
        projectId: Long?,
        cursor: String?,
        size: Int,
        includeProjects: Boolean,
        jdId: String? = null,
    ): ExperienceListResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        if (projectId != null) {
            experienceProjectReader.getProject(workspaceId = workspace.id, projectId = projectId)
        }

        val experiences = experienceReader.getExperiences(
            workspaceId = workspace.id,
            projectId = projectId,
            cursor = cursor,
            size = size,
        )

        val projects = if (includeProjects) {
            experienceProjectReader.getProjects(
                workspaceId = workspace.id,
                projectIds = experiences.items.map { it.projectId },
            ).mapValues { (_, project) -> ExperienceProjectResponse.from(project) }
        } else {
            emptyMap()
        }

        // jdId가 있으면 해당 JD 기준 지원 전략/매칭률/이유를 조인(경험 세트 변경 시 자동 재생성).
        // 매칭은 부가 정보이므로 재생성(AI 호출 등) 실패가 경험 목록 응답 자체를 깨지 않게 격리한다.
        val recommendation = jdId?.let {
            runCatching { getExperienceRecommendationService.getOrRefresh(workspace.id, it) }
                .onFailure { e -> log.warn(e) { "JD 매칭 조회 실패, 매칭 없이 응답: jdId=$jdId" } }
                .getOrNull()
        }
        val matchByExperienceId = recommendation?.items?.associateBy { it.experienceId }.orEmpty()

        return ExperienceListResponse(
            experiences = experiences.items.map { experience ->
                val match = matchByExperienceId[experience.id]
                ExperienceResponse.from(
                    experience = experience,
                    project = projects[experience.projectId],
                    matchRate = match?.matchRate,
                    reason = match?.reason,
                )
            },
            cursor = CursorResponse(nextCursor = experiences.nextCursor),
        )
    }

    fun searchExperiences(
        userId: Long,
        workspaceId: String,
        keyword: String,
        cursor: String?,
        size: Int,
        includeProjects: Boolean,
    ): ExperienceListResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        val experiences = experienceReader.searchExperiences(
            workspaceId = workspace.id,
            keyword = keyword,
            cursor = cursor,
            size = size,
        )

        val projects = if (includeProjects) {
            experienceProjectReader.getProjects(
                workspaceId = workspace.id,
                projectIds = experiences.items.map { it.projectId },
            ).mapValues { (_, project) -> ExperienceProjectResponse.from(project) }
        } else {
            emptyMap()
        }

        return ExperienceListResponse(
            experiences = experiences.items.map { experience ->
                ExperienceResponse.from(
                    experience = experience,
                    project = projects[experience.projectId],
                )
            },
            cursor = CursorResponse(nextCursor = experiences.nextCursor),
        )
    }

    private fun resolveContents(request: ExperienceContentsRequest): ExperienceContents {
        return when (request.type) {
            ExperienceContentsType.STAR -> request.toDomain()
            ExperienceContentsType.FREE -> experienceContentsPolishService.polishFreeStyleToStar(
                requireNotNull(request.free) { "FREE contents require free payload" }.content,
            )
        }
    }

}
