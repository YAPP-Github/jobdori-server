package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.experience.dto.request.CreateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.support.auth.Authenticated
import com.jobdori.api.support.auth.UserId
import com.jobdori.api.support.rest.ApiResponse
import com.jobdori.core.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.core.domain.experience.service.ExperienceProjectCreator
import com.jobdori.core.domain.experience.service.ExperienceProjectModifier
import com.jobdori.core.domain.experience.service.ExperienceProjectRemover
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ExperienceProjectController(
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val experienceProjectCreator: ExperienceProjectCreator,
    private val experienceProjectModifier: ExperienceProjectModifier,
    private val experienceProjectRemover: ExperienceProjectRemover,
) {

    @PostMapping("/api/v1/workspaces/{workspaceId}/experience-projects")
    @Authenticated
    fun createExperienceProject(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @RequestBody @Valid request: CreateExperienceProjectRequest,
    ): ApiResponse<ExperienceProjectResponse> {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val project = experienceProjectCreator.create(
            workspaceId = workspace.id,
            name = request.name,
            summary = request.summary,
            period = request.period?.toPeriod(),
            role = request.role,
        )

        return ApiResponse.ok(ExperienceProjectResponse.from(project))
    }

    @PatchMapping("/api/v1/workspaces/{workspaceId}/experience-projects/{projectId}")
    @Authenticated
    fun updateExperienceProject(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @PathVariable projectId: Long,
        @RequestBody @Valid request: UpdateExperienceProjectRequest,
    ): ApiResponse<ExperienceProjectResponse> {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val project = experienceProjectModifier.modify(
            workspaceId = workspace.id,
            projectId = projectId,
            name = request.name,
            summary = request.summary,
            period = request.period?.toPeriod(),
            role = request.role,
        )

        return ApiResponse.ok(ExperienceProjectResponse.from(project))
    }

    @DeleteMapping("/api/v1/workspaces/{workspaceId}/experience-projects/{projectId}")
    @Authenticated
    fun deleteExperienceProject(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @PathVariable projectId: Long,
    ): ApiResponse<Nothing?> {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        experienceProjectRemover.remove(
            workspaceId = workspace.id,
            projectId = projectId,
        )

        return ApiResponse.OK
    }

}
