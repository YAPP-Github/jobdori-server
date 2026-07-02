package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.experience.service.ExperiencePdfImportService
import com.jobdori.api.support.auth.Authenticated
import com.jobdori.api.support.auth.UserId
import com.jobdori.api.support.rest.ApiResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class ExperienceImportPdfController(
    private val experiencePdfImportService: ExperiencePdfImportService,
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

}
