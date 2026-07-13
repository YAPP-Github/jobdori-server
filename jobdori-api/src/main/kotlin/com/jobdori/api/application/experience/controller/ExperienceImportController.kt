package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.experience.dto.request.NotionExperienceImportRequest
import com.jobdori.api.application.experience.service.ExperiencePdfImportService
import com.jobdori.api.application.notion.service.NotionExperienceImportService
import com.jobdori.api.support.auth.Authenticated
import com.jobdori.api.support.auth.UserId
import com.jobdori.api.support.rest.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class ExperienceImportController(
    private val experiencePdfImportService: ExperiencePdfImportService,
    private val notionExperienceImportService: NotionExperienceImportService,
) {

    @Authenticated
    @PostMapping(
        "/v1/workspaces/{workspaceId}/experience-imports",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun importExperiences(
        @RequestPart file: MultipartFile,
        @PathVariable workspaceId: String,
        @UserId userId: Long,
    ): ApiResponse<Nothing?> {
        experiencePdfImportService.importExperiences(file = file, workspaceId = workspaceId, userId = userId)
        return ApiResponse.OK
    }

    @Authenticated
    @PostMapping("/v1/workspaces/{workspaceId}/experience-imports/notion")
    fun importNotionExperiences(
        @RequestBody @Valid request: NotionExperienceImportRequest,
        @PathVariable workspaceId: String,
        @UserId userId: Long,
    ): ApiResponse<Nothing?> {
        notionExperienceImportService.importExperiences(
            userId = userId,
            workspaceId = workspaceId,
            connectionId = request.connectionId,
            pageId = request.pageId,
        )
        return ApiResponse.OK
    }

}
