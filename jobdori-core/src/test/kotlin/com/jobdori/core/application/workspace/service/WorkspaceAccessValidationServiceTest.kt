package com.jobdori.core.application.workspace.service

import com.jobdori.core.domain.workspace.WorkspaceFixture
import com.jobdori.core.domain.workspace.error.WorkspaceAccessDeniedException
import com.jobdori.core.domain.workspace.service.WorkspaceReader
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class WorkspaceAccessValidationServiceTest : StringSpec({

    val workspaceReader = mockk<WorkspaceReader>()
    val workspaceAccessValidationService = WorkspaceAccessValidationService(workspaceReader)

    "워크스페이스가 존재하고 소유자이면 워크스페이스를 반환한다" {
        // given
        val workspace = WorkspaceFixture.create(
            id = 1L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            ownerUserId = 10L,
        )
        every { workspaceReader.getWorkspace("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns workspace

        // when & then
        workspaceAccessValidationService.validateAccessible(
            workspacePublicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            userId = 10L,
        ) shouldBe workspace
    }

    "워크스페이스 소유자가 아니면 예외가 발생한다" {
        // given
        every { workspaceReader.getWorkspace("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns WorkspaceFixture.create(
            id = 1L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            ownerUserId = 20L,
        )

        // when & then
        shouldThrow<WorkspaceAccessDeniedException> {
            workspaceAccessValidationService.validateAccessible(
                workspacePublicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
                userId = 10L,
            )
        }
    }

})
