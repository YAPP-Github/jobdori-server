package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.experience.dto.request.ListExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.ListExperienceRequest
import com.jobdori.api.application.experience.dto.request.SearchExperienceRequest
import com.jobdori.api.application.experience.dto.response.ExperienceListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectListResponse
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
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
    ): ExperienceResponse? = experienceService.getExperience(
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
    ): ExperienceListResponse = experienceService.getExperiences(
        userId = userId,
        workspaceId = workspaceId,
        projectId = projectId,
        cursor = cursorRequest.cursor,
        size = cursorRequest.size,
        includeProjects = env.selectionSet.contains("experiences/project"),
    )

    @QueryMapping
    fun searchExperiences(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Arguments request: SearchExperienceRequest,
        env: DataFetchingEnvironment,
    ): ExperienceListResponse = experienceService.searchExperiences(
        userId = userId,
        workspaceId = workspaceId,
        keyword = request.keyword,
        cursor = request.cursor,
        size = request.size,
        includeProjects = env.selectionSet.contains("experiences/project"),
    )

    @QueryMapping
    fun experienceProjects(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Arguments cursorRequest: ListExperienceProjectRequest,
        env: DataFetchingEnvironment,
    ): ExperienceProjectListResponse = experienceProjectService.getProjects(
        userId = userId,
        workspaceId = workspaceId,
        cursor = cursorRequest.cursor,
        size = cursorRequest.size,
        includeExperienceCount = env.selectionSet.contains("projects/experienceCount"),
    )

    @QueryMapping
    fun experienceProject(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument id: Long,
        env: DataFetchingEnvironment,
    ): ExperienceProjectResponse = experienceProjectService.getProject(
        userId = userId,
        workspaceId = workspaceId,
        projectId = id,
        includeExperienceCount = env.selectionSet.contains("experienceCount"),
    )

}
