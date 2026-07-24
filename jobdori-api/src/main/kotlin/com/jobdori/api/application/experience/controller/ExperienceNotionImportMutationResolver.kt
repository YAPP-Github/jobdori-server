package com.jobdori.api.application.experience.controller

import com.jobdori.api.application.experience.dto.request.NotionExperienceImportRequest
import com.jobdori.api.application.experience.service.NotionExperienceImportService
import com.jobdori.api.support.auth.UserId
import jakarta.validation.Valid
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class ExperienceNotionImportMutationResolver(
    private val notionExperienceImportService: NotionExperienceImportService,
) {

    @MutationMapping
    fun importNotionExperiences(
        @UserId userId: Long,
        @Argument workspaceId: String,
        @Valid @Argument request: NotionExperienceImportRequest,
    ): Boolean {
        notionExperienceImportService.importExperiences(
            userId = userId,
            workspaceId = workspaceId,
            connectionId = request.connectionId,
            pageId = request.pageId,
        )
        return true
    }

}
