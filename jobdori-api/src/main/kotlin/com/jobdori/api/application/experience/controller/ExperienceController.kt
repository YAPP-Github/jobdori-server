package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.experience.dto.request.CreateExperienceRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceRequest
import com.jobdori.api.application.experience.dto.response.ExperienceResponse
import com.jobdori.api.application.experience.service.ExperienceService
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
class ExperienceController(
    private val experienceService: ExperienceService,
) {

    @PostMapping("/v1/workspaces/{workspaceId}/experiences")
    @Authenticated
    fun createExperience(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @RequestBody @Valid request: CreateExperienceRequest,
    ): ApiResponse<ExperienceResponse> {
        val response = experienceService.createExperience(
            userId = userId,
            workspaceId = workspaceId,
            projectId = request.projectId,
            tags = request.tags,
            title = request.title,
            contents = request.contents.toDomain(),
        )

        return ApiResponse.ok(response)
    }

    @PatchMapping("/v1/workspaces/{workspaceId}/experiences/{experienceId}")
    @Authenticated
    fun updateExperience(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @PathVariable experienceId: Long,
        @RequestBody @Valid request: UpdateExperienceRequest,
    ): ApiResponse<ExperienceResponse> {
        val response = experienceService.modifyExperience(
            userId = userId,
            workspaceId = workspaceId,
            experienceId = experienceId,
            projectId = request.projectId,
            tags = request.tags,
            title = request.title,
            contents = request.contents?.toDomain(),
        )

        return ApiResponse.ok(response)
    }

    @DeleteMapping("/v1/workspaces/{workspaceId}/experiences/{experienceId}")
    @Authenticated
    fun deleteExperience(
        @UserId userId: Long,
        @PathVariable workspaceId: String,
        @PathVariable experienceId: Long,
    ): ApiResponse<Nothing?> {
        experienceService.removeExperience(
            userId = userId,
            workspaceId = workspaceId,
            experienceId = experienceId,
        )

        return ApiResponse.OK
    }

}
