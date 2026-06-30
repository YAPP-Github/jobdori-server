package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.experience.dto.request.CreateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.response.ExperienceProjectResponse
import com.jobdori.api.application.experience.service.ExperienceProjectService
import com.jobdori.api.support.auth.Authenticated
import com.jobdori.api.support.auth.UserId
import com.jobdori.api.support.rest.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ExperienceProjectController(
    private val experienceProjectService: ExperienceProjectService,
) {

    @PostMapping("/api/v1/workspaces/{workspaceId}/experience-projects")
    @Authenticated
    fun createExperienceProject(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @RequestBody @Valid request: CreateExperienceProjectRequest,
    ): ApiResponse<ExperienceProjectResponse> {
        val response = experienceProjectService.create(
            userId = userId,
            workspaceId = workspaceId,
            name = request.name,
            summary = request.summary,
            period = request.period?.toPeriod(),
            role = request.role,
        )

        return ApiResponse.ok(response)
    }

    @PatchMapping("/api/v1/workspaces/{workspaceId}/experience-projects/{projectId}")
    @Authenticated
    fun updateExperienceProject(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @PathVariable projectId: Long,
        @RequestBody @Valid request: UpdateExperienceProjectRequest,
    ): ApiResponse<ExperienceProjectResponse> {
        val response = experienceProjectService.modify(
            userId = userId,
            workspaceId = workspaceId,
            projectId = projectId,
            name = request.name,
            summary = request.summary,
            period = request.period?.toPeriod(),
            role = request.role,
        )

        return ApiResponse.ok(response)
    }

    @DeleteMapping("/api/v1/workspaces/{workspaceId}/experience-projects/{projectId}")
    @Authenticated
    fun deleteExperienceProject(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @PathVariable projectId: Long,
    ): ApiResponse<Nothing?> {
        experienceProjectService.remove(
            userId = userId,
            workspaceId = workspaceId,
            projectId = projectId,
        )

        return ApiResponse.OK
    }

}
