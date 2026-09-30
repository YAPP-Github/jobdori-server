package com.jobdori.api.application.resume.controller

import com.jobdori.api.application.resume.dto.response.ResumeResponse
import com.jobdori.api.application.resume.service.PdfResumeImportService
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
class PdfResumeImportController(
    private val pdfResumeImportService: PdfResumeImportService,
) {

    @Authenticated
    @PostMapping(
        "/v1/workspaces/{workspaceId}/resume-imports",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    fun importResume(
        @RequestPart file: MultipartFile,
        @PathVariable workspaceId: String,
        @UserId userId: Long,
    ): ApiResponse<ResumeResponse> = ApiResponse.ok(
        pdfResumeImportService.importResume(
            file = file,
            workspaceId = workspaceId,
            userId = userId,
        ),
    )

}
