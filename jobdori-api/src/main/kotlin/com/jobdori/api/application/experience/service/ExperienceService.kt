package com.jobdori.api.application.experience.service

import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.api.application.experience.dto.request.CreateExperienceRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceRequest
import com.jobdori.api.application.experience.dto.request.contents.ExperienceContentsRequest
import com.jobdori.api.application.experience.dto.response.ExperienceListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.api.application.experience.dto.request.ExperienceListTab
import com.jobdori.common.model.Period
import com.jobdori.core.application.experience.ExperienceContentsPolishService
import com.jobdori.core.application.experience.PolishedExperience
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
    private val experienceRecommendationListService: ExperienceRecommendationListService,
    private val experienceContentsPolishService: ExperienceContentsPolishService,
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
        val resolvedContents = resolveContents(request.contents)
        val experience = experienceCreator.create(
            workspaceId = workspace.id,
            projectId = projectId,
            command = ExperienceCreateCommand(
                title = request.title.ifBlank { resolvedContents.title.orEmpty() },
                contents = resolvedContents.contents,
                tags = request.tags.ifEmpty { resolvedContents.tags },
                period = request.period?.toPeriod() ?: resolvedContents.period ?: project.period,
                role = request.role?.ifBlank { resolvedContents.role ?: project.role }
            ),
        )

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

        val resolvedContents = resolveContents(request.contents)
        val modified = experienceModifier.modify(
            workspaceId = workspace.id,
            experienceId = experienceId,
            projectId = request.projectId,
            tags = request.tags,
            title = request.title.ifBlank { resolvedContents.title.orEmpty() },
            contents = resolvedContents.contents,
            period = request.period?.toPeriod() ?: resolvedContents.period,
            role = request.role ?: resolvedContents.role,
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
        questionRoomId: String? = null,
        tab: ExperienceListTab? = null,
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

        // AI_RECOMMENDATION 탭은 순서와 커서가 달라 별도 응답을 만든다. 위의 기본 조회 결과는 이 경우 쓰지 않는다.
        experienceRecommendationListService.getList(
            workspaceId = workspace.id,
            jdId = jdId,
            questionRoomId = questionRoomId,
            tab = tab,
            projectId = projectId,
            cursor = cursor,
            size = size,
            includeProjects = includeProjects,
        )?.let { return it }

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

    private fun resolveContents(request: ExperienceContentsRequest): ResolvedExperienceContents {
        return when (request.type) {
            ExperienceContentsType.STAR -> ResolvedExperienceContents(contents = request.toDomain())
            ExperienceContentsType.FREE -> experienceContentsPolishService.polishFreeStyleToStar(
                requireNotNull(request.free) { "FREE contents require free payload" }.content,
            ).toResolvedContents()
        }
    }

}

private data class ResolvedExperienceContents(
    val title: String? = null,
    val period: Period? = null,
    val role: String? = null,
    val tags: List<String> = emptyList(),
    val contents: ExperienceContents,
)

private fun PolishedExperience.toResolvedContents() = ResolvedExperienceContents(
    title = title,
    period = period,
    role = role,
    tags = tags,
    contents = contents,
)
