package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.experience.dto.request.ListExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.ListExperienceRequest
import com.jobdori.api.application.experience.dto.response.ExperienceListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.api.application.experience.service.ExperienceProjectService
import com.jobdori.api.application.experience.service.ExperienceService
import com.jobdori.api.support.auth.UserId
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.Arguments
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ExperienceQueryResolver(
    private val experienceService: ExperienceService,
    private val experienceProjectService: ExperienceProjectService,
) {

    @QueryMapping
    fun experience(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument id: Long,
        env: DataFetchingEnvironment,
    ): ExperienceResponse? = experienceService.get(
        userId = userId,
        workspaceId = workspaceId,
        experienceId = id,
        includeProject = env.selectionSet.contains("project"),
    )

    @QueryMapping
    fun experiences(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument projectId: Long?,
        @Arguments cursorRequest: ListExperienceRequest,
        env: DataFetchingEnvironment,
    ): ExperienceListResponse = experienceService.getAll(
        userId = userId,
        workspaceId = workspaceId,
        projectId = projectId,
        cursor = cursorRequest.cursor,
        size = cursorRequest.size,
        includeProjects = env.selectionSet.contains("experiences/project"),
    )

    @QueryMapping
    fun experienceProjects(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Arguments cursorRequest: ListExperienceProjectRequest,
    ): ExperienceProjectListResponse = experienceProjectService.getAll(
        userId = userId,
        workspaceId = workspaceId,
        cursor = cursorRequest.cursor,
        size = cursorRequest.size,
    )

}
