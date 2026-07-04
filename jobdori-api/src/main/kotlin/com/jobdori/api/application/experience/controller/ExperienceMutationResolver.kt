package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.experience.dto.request.CreateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.CreateExperienceRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceRequest
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.api.application.experience.service.ExperienceProjectService
import com.jobdori.api.application.experience.service.ExperienceService
import com.jobdori.api.support.auth.UserId
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class ExperienceMutationResolver(
    private val experienceService: ExperienceService,
    private val experienceProjectService: ExperienceProjectService,
) {

    @MutationMapping
    fun createExperience(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Valid @Argument request: CreateExperienceRequest,
    ): ExperienceResponse = experienceService.createExperience(
        userId = userId,
        workspaceId = workspaceId,
        projectId = request.projectId,
        tags = request.tags,
        title = request.title,
        contents = request.contents.toDomain(),
    )

    @MutationMapping
    fun updateExperience(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument experienceId: Long,
        @Valid @Argument request: UpdateExperienceRequest,
    ): ExperienceResponse = experienceService.modifyExperience(
        userId = userId,
        workspaceId = workspaceId,
        experienceId = experienceId,
        projectId = request.projectId,
        tags = request.tags,
        title = request.title,
        contents = request.contents?.toDomain(),
    )

    @MutationMapping
    fun deleteExperience(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument experienceId: Long,
    ): Boolean {
        experienceService.removeExperience(
            userId = userId,
            workspaceId = workspaceId,
            experienceId = experienceId,
        )
        return true
    }

    @MutationMapping
    fun createExperienceProject(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Valid @Argument input: CreateExperienceProjectRequest,
    ): ExperienceProjectResponse = experienceProjectService.createProject(
        userId = userId,
        workspaceId = workspaceId,
        name = input.name,
        summary = input.summary,
        period = input.period?.toPeriod(),
        role = input.role,
    )

    @MutationMapping
    fun updateExperienceProject(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument projectId: Long,
        @Valid @Argument request: UpdateExperienceProjectRequest,
    ): ExperienceProjectResponse = experienceProjectService.modifyProject(
        userId = userId,
        workspaceId = workspaceId,
        projectId = projectId,
        name = request.name,
        summary = request.summary,
        period = request.period?.toPeriod(),
        role = request.role,
    )

    @MutationMapping
    fun deleteExperienceProject(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Argument projectId: Long,
    ): Boolean {
        experienceProjectService.removeProject(
            userId = userId,
            workspaceId = workspaceId,
            projectId = projectId,
        )
        return true
    }

}
