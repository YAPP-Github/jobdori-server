package com.jobdori.api.application.experience.service

import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.credit.CreditService
import com.jobdori.core.application.notion.NotionPageService
import com.jobdori.core.domain.credit.CreditFeature
import org.springframework.stereotype.Service

@Service
class NotionExperienceImportService(
    private val notionPageService: NotionPageService,
    private val workspaceAccessValidationService: WorkspaceAccessValidationService,
    private val creditService: CreditService,
    private val experienceTextImportService: ExperienceTextImportService,
) {

    fun importExperiences(
        userId: Long,
        workspaceId: String,
        connectionId: Long,
        pageId: String,
    ) {
        val workspace = workspaceAccessValidationService.validateAccessible(
            workspaceId = workspaceId,
            userId = userId,
        )
        val content = notionPageService.getPageContent(
            workspaceId = workspace.id,
            connectionId = connectionId,
            pageId = pageId,
        )
        val text = content.plainText.trim()
        if (text.isBlank()) {
            throw InvalidArgumentsException(
                message = "Notion 페이지에서 가져올 텍스트가 없습니다 [userId=$userId,pageId=$pageId]",
            )
        }

        creditService.consume(userId, CreditFeature.EXPERIENCE_IMPORT)
        experienceTextImportService.import(
            workspaceId = workspace.id,
            text = text,
        )
    }

}
