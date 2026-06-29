package com.jobdori.core.domain.workspace.service

import com.jobdori.core.domain.workspace.Workspace
import com.jobdori.core.domain.workspace.repository.WorkspaceRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class WorkspaceCreatorTest : StringSpec({

    val workspaceRepository = mockk<WorkspaceRepository>()
    val service = WorkspaceCreator(workspaceRepository)

    "새로운 워크스페이스를 등록한다" {
        // given
        val workspaceSlot = slot<Workspace>()
        every { workspaceRepository.save(capture(workspaceSlot)) } returns Workspace(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            ownerUserId = 1L,
        )

        // when & then
        service.create(ownerUserId = 1L) shouldBe Workspace(
            id = 10L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            ownerUserId = 1L,
        )

        // then
        workspaceSlot.captured.id shouldBe 0L
        workspaceSlot.captured.ownerUserId shouldBe 1L
        verify(exactly = 1) { workspaceRepository.save(any()) }
    }

})
