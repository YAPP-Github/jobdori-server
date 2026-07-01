package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.experience.service.ExperienceImportService
import com.jobdori.api.support.auth.Authenticated
import com.jobdori.api.support.auth.UserId
import com.jobdori.api.support.rest.ApiResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class ExperienceImportController(
    private val experienceImportService: ExperienceImportService,
) {

    @Authenticated
    @PostMapping("/v1/experiences/imports/pdf", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun importExperiencesByPdf(
        @RequestPart file: MultipartFile,
        @UserId userId: Long,
    ): ApiResponse<Nothing?> {
        experienceImportService.importExperiencesByPdf(file = file, userId = userId)
        return ApiResponse.OK
    }

}
