package com.jobdori.api.application.experience.service

import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.model.Period
import com.jobdori.core.domain.experience.service.ExperienceProjectCreator
import com.jobdori.core.domain.experience.service.ExperienceProjectModifier
import com.jobdori.core.domain.experience.service.ExperienceProjectReader
import com.jobdori.core.domain.experience.service.ExperienceProjectRemover
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand
import org.springframework.stereotype.Service

@Service
class ExperienceProjectService(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val experienceProjectCreator: ExperienceProjectCreator,
    private val experienceProjectReader: ExperienceProjectReader,
    private val experienceProjectModifier: ExperienceProjectModifier,
    private val experienceProjectRemover: ExperienceProjectRemover,
    private val experienceReader: ExperienceReader,
) {

    fun createProject(
        userId: Long,
        workspaceId: String,
        name: String,
        summary: String,
        period: Period?,
        role: String?,
    ): ExperienceProjectResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val project = experienceProjectCreator.create(
            workspaceId = workspace.id,
            command = ExperienceProjectCreateCommand(
                name = name,
                summary = summary,
                period = period,
                role = role,
            ),
        )

        return ExperienceProjectResponse.from(project)
    }

    fun modifyProject(
        userId: Long,
        workspaceId: String,
        projectId: Long,
        name: String?,
        summary: String?,
        period: Period?,
        role: String?,
    ): ExperienceProjectResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val project = experienceProjectModifier.modify(
            workspaceId = workspace.id,
            projectId = projectId,
            name = name,
            summary = summary,
            period = period,
            role = role,
        )

        return ExperienceProjectResponse.from(project)
    }

    fun removeProject(userId: Long, workspaceId: String, projectId: Long) {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        experienceProjectRemover.remove(
            workspaceId = workspace.id,
            projectId = projectId,
        )
    }


    fun getProjects(
        userId: Long,
        workspaceId: String,
        cursor: String?,
        size: Int,
        includeExperienceCount: Boolean = false,
    ): ExperienceProjectListResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val result = experienceProjectReader.getProjects(
            workspaceId = workspace.id,
            cursor = cursor,
            size = size,
        )
        val experienceCounts = if (includeExperienceCount) {
            experienceReader.getExperienceCountsByProjectIds(
                workspaceId = workspace.id,
                projectIds = result.items.map { it.id },
            )
        } else {
            emptyMap()
        }

        return ExperienceProjectListResponse(
            projects = result.items.map { project ->
                ExperienceProjectResponse.from(
                    project = project,
                    experienceCount = experienceCounts[project.id]?.toInt() ?: if (includeExperienceCount) 0 else null,
                )
            },
            cursor = CursorResponse(nextCursor = result.nextCursor),
        )
    }

    fun getProject(
        userId: Long,
        workspaceId: String,
        projectId: Long,
        includeExperienceCount: Boolean = false,
    ): ExperienceProjectResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val project = experienceProjectReader.getProject(
            workspaceId = workspace.id,
            projectId = projectId,
        )
        val experienceCount = if (includeExperienceCount) {
            experienceReader.getExperienceCountsByProjectIds(
                workspaceId = workspace.id,
                projectIds = listOf(project.id),
            )[project.id]?.toInt() ?: 0
        } else {
            null
        }

        return ExperienceProjectResponse.from(
            project = project,
            experienceCount = experienceCount,
        )
    }

}
