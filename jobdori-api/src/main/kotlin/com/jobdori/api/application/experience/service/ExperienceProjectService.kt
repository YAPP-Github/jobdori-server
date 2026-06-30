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
import org.springframework.stereotype.Service

@Service
class ExperienceProjectService(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val experienceProjectCreator: ExperienceProjectCreator,
    private val experienceProjectReader: ExperienceProjectReader,
    private val experienceProjectModifier: ExperienceProjectModifier,
    private val experienceProjectRemover: ExperienceProjectRemover,
) {

    fun create(
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
            name = name,
            summary = summary,
            period = period,
            role = role,
        )

        return ExperienceProjectResponse.from(project)
    }

    fun modify(
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

    fun getAll(
        userId: Long,
        workspaceId: String,
        cursor: String?,
        size: Int,
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

        return ExperienceProjectListResponse(
            projects = result.items.map(ExperienceProjectResponse::from),
            cursor = CursorResponse(nextCursor = result.nextCursor),
        )
    }

    fun remove(userId: Long, workspaceId: String, projectId: Long) {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )

        experienceProjectRemover.remove(
            workspaceId = workspace.id,
            projectId = projectId,
        )
    }

}
