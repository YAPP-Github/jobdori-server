package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.api.application.experience.dto.request.ListExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.ListExperienceRequest
import com.jobdori.api.application.experience.dto.response.ExperienceListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.api.application.experience.service.ExperienceService
import com.jobdori.api.support.auth.UserId
import com.jobdori.core.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.core.domain.experience.service.ExperienceProjectReader
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.Arguments
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ExperienceQueryResolver(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val experienceService: ExperienceService,
    private val experienceProjectReader: ExperienceProjectReader,
) {

    @QueryMapping
    fun experience(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument id: Long,
        env: DataFetchingEnvironment,
    ): ExperienceResponse? {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        return experienceService.get(
            workspaceId = workspace.id,
            experienceId = id,
            includeProject = env.selectionSet.contains("project"),
        )
    }

    @QueryMapping
    fun experiences(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument projectId: Long?,
        @Arguments cursorRequest: ListExperienceRequest,
        env: DataFetchingEnvironment,
    ): ExperienceListResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        return experienceService.getAll(
            workspaceId = workspace.id,
            projectId = projectId,
            cursor = cursorRequest.cursor,
            size = cursorRequest.size,
            includeProjects = env.selectionSet.contains("experiences/project"),
        )
    }

    @QueryMapping
    fun experienceProjects(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Arguments cursorRequest: ListExperienceProjectRequest,
    ): ExperienceProjectListResponse {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val result = experienceProjectReader.getProjects(
            workspaceId = workspace.id,
            cursor = cursorRequest.cursor,
            size = cursorRequest.size,
        )

        return ExperienceProjectListResponse(
            projects = result.items.map(ExperienceProjectResponse::from),
            cursor = CursorResponse(nextCursor = result.nextCursor),
        )
    }

}
