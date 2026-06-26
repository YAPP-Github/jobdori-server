package com.jobdori.core.domain.workspace.service

import com.jobdori.core.domain.workspace.WorkspaceFixture
import com.jobdori.core.domain.workspace.error.WorkspaceNotFoundException
import com.jobdori.core.domain.workspace.repository.WorkspaceRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class WorkspaceReaderTest : StringSpec({

    val workspaceRepository = mockk<WorkspaceRepository>()
    val workspaceReader = WorkspaceReader(workspaceRepository)

    "워크스페이스 publicId로 워크스페이스를 조회한다" {
        // given
        val workspace = WorkspaceFixture.create(
            id = 1L,
            publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6",
            ownerUserId = 10L,
        )
        every { workspaceRepository.findByPublicId("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns workspace

        // when & then
        workspaceReader.getWorkspace(publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6") shouldBe workspace
    }

    "워크스페이스 publicId로 조회할 때 없으면 예외가 발생한다" {
        // given
        every { workspaceRepository.findByPublicId("3f5c9d79-2255-4b76-bd31-013cd01d49d6") } returns null

        // when & then
        shouldThrow<WorkspaceNotFoundException> {
            workspaceReader.getWorkspace(publicId = "3f5c9d79-2255-4b76-bd31-013cd01d49d6")
        }
    }

    "소유자 유저 ID로 워크스페이스 목록을 조회한다" {
        // given
        val workspaces = listOf(WorkspaceFixture.create(id = 1L, ownerUserId = 10L))
        every { workspaceRepository.findAllByOwnerUserId(10L) } returns workspaces

        // when & then
        workspaceReader.getWorkspaces(ownerUserId = 10L) shouldBe workspaces
    }

    "워크스페이스가 없으면 빈 목록을 반환한다" {
        // given
        every { workspaceRepository.findAllByOwnerUserId(10L) } returns emptyList()

        // when & then
        workspaceReader.getWorkspaces(ownerUserId = 10L) shouldBe emptyList()
    }

})
