package com.jobdori.api.application.notion.service

import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.experience.ExperienceAiExtractionService
import com.jobdori.core.application.experience.ExperienceImportService
import com.jobdori.core.application.experience.command.ImportedExperienceCommandGroup
import com.jobdori.core.application.notion.NotionPageService
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand
import com.jobdori.core.domain.notion.NotionPageContent
import com.jobdori.core.domain.notion.NotionPageSummary
import com.jobdori.core.domain.workspace.Workspace
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class NotionExperienceImportServiceTest : StringSpec({

    val notionPageService = mockk<NotionPageService>()
    val experienceAiExtractionService = mockk<ExperienceAiExtractionService>()
    val experienceImportService = mockk<ExperienceImportService>()
    val workspaceAccessValidationService = mockk<WorkspaceAccessValidationService>()
    val service = NotionExperienceImportService(
        notionPageService = notionPageService,
        experienceAiExtractionService = experienceAiExtractionService,
        experienceImportService = experienceImportService,
        workspaceAccessValidationService = workspaceAccessValidationService,
    )

    beforeTest {
        clearMocks(
            notionPageService,
            experienceAiExtractionService,
            experienceImportService,
            workspaceAccessValidationService,
        )
        every {
            workspaceAccessValidationService.validateAccessible(
                workspaceId = "workspace-public-id",
                userId = 1L,
            )
        } returns Workspace(
            id = 10L,
            publicId = "workspace-public-id",
            ownerUserId = 1L,
        )
    }

    "Notion 페이지 본문을 AI로 추출한 뒤 경험으로 저장한다" {
        // given
        val content = NotionPageContent(
            page = NotionPageSummary(id = "page-id", title = "Resume", url = null, lastEditedTime = null),
            plainText = "  Notion resume text  ",
            blocks = emptyList(),
        )
        val groups = listOf(
            ImportedExperienceCommandGroup(
                project = ExperienceProjectCreateCommand(
                    name = "Notion 경험 가져오기",
                    summary = "Notion 페이지에서 추출한 경험",
                    period = null,
                    role = null,
                ),
                experiences = listOf(
                    ExperienceCreateCommand(
                        tags = listOf("Notion"),
                        title = "Notion 페이지 가져오기",
                        contents = ExperienceContents.free("Notion 페이지 본문을 가져왔다"),
                    ),
                ),
            ),
        )
        every {
            notionPageService.getPageContent(
                workspaceId = 10L,
                connectionId = 1,
                pageId = "page-id",
            )
        } returns content
        every { experienceAiExtractionService.extract("Notion resume text") } returns groups
        every { experienceImportService.saveAll(workspaceId = 10L, groups = groups) } returns Unit

        // when
        service.importExperiences(
            userId = 1L,
            workspaceId = "workspace-public-id",
            connectionId = 1,
            pageId = "page-id",
        )

        // then
        verify(exactly = 1) {
            workspaceAccessValidationService.validateAccessible(
                workspaceId = "workspace-public-id",
                userId = 1L,
            )
        }
        verify(exactly = 1) { experienceAiExtractionService.extract("Notion resume text") }
        verify(exactly = 1) { experienceImportService.saveAll(workspaceId = 10L, groups = groups) }
    }

    "Notion 페이지 본문이 비어 있으면 경험을 저장하지 않는다" {
        // given
        every {
            notionPageService.getPageContent(
                workspaceId = 10L,
                connectionId = 1,
                pageId = "empty-page-id",
            )
        } returns NotionPageContent(
            page = NotionPageSummary(id = "empty-page-id", title = "Empty", url = null, lastEditedTime = null),
            plainText = " \n\t ",
            blocks = emptyList(),
        )

        // when
        val exception = shouldThrow<InvalidArgumentsException> {
            service.importExperiences(
                userId = 1L,
                workspaceId = "workspace-public-id",
                connectionId = 1,
                pageId = "empty-page-id",
            )
        }

        // then
        exception.message shouldBe "Notion 페이지에서 가져올 텍스트가 없습니다 [userId=1,pageId=empty-page-id]"
        verify(exactly = 0) { experienceAiExtractionService.extract(any()) }
        verify(exactly = 0) { experienceImportService.saveAll(workspaceId = any(), groups = any()) }
    }

})
