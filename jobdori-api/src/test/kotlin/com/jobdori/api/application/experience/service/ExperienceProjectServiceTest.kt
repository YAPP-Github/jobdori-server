package com.jobdori.api.application.experience.service

import com.jobdori.api.application.common.dto.request.PeriodRequest
import com.jobdori.api.application.experience.dto.request.CreateExperienceProjectRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceProjectRequest
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.model.Period
import com.jobdori.common.model.SliceResult
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.service.ExperienceProjectCreator
import com.jobdori.core.domain.experience.service.ExperienceProjectModifier
import com.jobdori.core.domain.experience.service.ExperienceProjectReader
import com.jobdori.core.domain.experience.service.ExperienceProjectRemover
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand
import com.jobdori.core.domain.workspace.Workspace
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate

class ExperienceProjectServiceTest : StringSpec({

    val workspaceAccessValidationService = mockk<WorkspaceAccessValidationService>()
    val experienceProjectCreator = mockk<ExperienceProjectCreator>()
    val experienceProjectReader = mockk<ExperienceProjectReader>()
    val experienceProjectModifier = mockk<ExperienceProjectModifier>()
    val experienceProjectRemover = mockk<ExperienceProjectRemover>()
    val experienceReader = mockk<ExperienceReader>()
    val experienceProjectService = ExperienceProjectService(
        workspaceAccessValidationService = workspaceAccessValidationService,
        experienceProjectCreator = experienceProjectCreator,
        experienceProjectReader = experienceProjectReader,
        experienceProjectModifier = experienceProjectModifier,
        experienceProjectRemover = experienceProjectRemover,
        experienceReader = experienceReader,
    )

    beforeTest {
        every {
            workspaceAccessValidationService.validateAccessible(
                workspaceId = "workspace-id",
                userId = 10L,
            )
        } returns Workspace(
            id = 1L,
            publicId = "workspace-id",
            ownerUserId = 10L,
        )
    }

    "프로젝트 생성 요청 모델을 command로 변환해 생성한다" {
        // given
        val request = CreateExperienceProjectRequest(
            name = "프로젝트",
            summary = "프로젝트 요약",
            period = PeriodRequest(
                startAt = LocalDate.of(2025, 1, 1),
                endAt = LocalDate.of(2025, 3, 31),
            ),
            role = "백엔드",
        )
        every {
            experienceProjectCreator.create(
                workspaceId = 1L,
                command = ExperienceProjectCreateCommand(
                    name = "프로젝트",
                    summary = "프로젝트 요약",
                    period = Period(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 3, 31)),
                    role = "백엔드",
                ),
            )
        } returns project(3L)

        // when
        val response = experienceProjectService.createProject(
            userId = 10L,
            workspaceId = "workspace-id",
            request = request,
        )

        // then
        response.projectId shouldBe 3L
    }

    "프로젝트 수정 요청 모델을 수정 인자로 변환한다" {
        // given
        val request = UpdateExperienceProjectRequest(
            name = "수정 프로젝트",
            summary = null,
            period = PeriodRequest(
                startAt = LocalDate.of(2025, 4, 1),
                endAt = LocalDate.of(2025, 6, 30),
            ),
            role = "리드",
        )
        every {
            experienceProjectModifier.modify(
                workspaceId = 1L,
                projectId = 3L,
                name = "수정 프로젝트",
                summary = null,
                period = Period(LocalDate.of(2025, 4, 1), LocalDate.of(2025, 6, 30)),
                role = "리드",
            )
        } returns project(3L)

        // when
        val response = experienceProjectService.modifyProject(
            userId = 10L,
            workspaceId = "workspace-id",
            projectId = 3L,
            request = request,
        )

        // then
        response.projectId shouldBe 3L
    }

    "프로젝트 목록에서 experienceCount를 요청하면 프로젝트별 경험 개수를 채운다" {
        // given
        val projects = listOf(project(3L), project(4L))
        every {
            experienceProjectReader.getProjects(
                workspaceId = 1L,
                cursor = null,
                size = 2,
            )
        } returns SliceResult(items = projects, nextCursor = null)
        every {
            experienceReader.getExperienceCountsByProjectIds(
                workspaceId = 1L,
                projectIds = listOf(3L, 4L),
            )
        } returns mapOf(3L to 5L)

        // when
        val response = experienceProjectService.getProjects(
            userId = 10L,
            workspaceId = "workspace-id",
            cursor = null,
            size = 2,
            includeExperienceCount = true,
        )

        // then
        response.projects.map { it.experienceCount } shouldContainExactly listOf(5L, 0L)
    }

    "프로젝트 목록에서 experienceCount를 요청하지 않으면 경험 개수를 조회하지 않는다" {
        // given
        every {
            experienceProjectReader.getProjects(
                workspaceId = 1L,
                cursor = null,
                size = 2,
            )
        } returns SliceResult(items = listOf(project(3L)), nextCursor = null)

        // when
        val response = experienceProjectService.getProjects(
            userId = 10L,
            workspaceId = "workspace-id",
            cursor = null,
            size = 2,
            includeExperienceCount = false,
        )

        // then
        response.projects.single().experienceCount.shouldBeNull()
        verify(exactly = 0) { experienceReader.getExperienceCountsByProjectIds(any(), any()) }
    }

    "프로젝트 단건에서 experienceCount를 요청하면 경험 개수를 채운다" {
        // given
        every { experienceProjectReader.getProject(workspaceId = 1L, projectId = 3L) } returns project(3L)
        every {
            experienceReader.getExperienceCountsByProjectIds(
                workspaceId = 1L,
                projectIds = listOf(3L),
            )
        } returns mapOf(3L to 7L)

        // when
        val response = experienceProjectService.getProject(
            userId = 10L,
            workspaceId = "workspace-id",
            projectId = 3L,
            includeExperienceCount = true,
        )

        // then
        response.projectId shouldBe 3L
        response.experienceCount shouldBe 7L
    }

})

private fun project(id: Long) = ExperienceProject(
    id = id,
    workspaceId = 1L,
    name = "프로젝트 $id",
    summary = "프로젝트 요약",
    period = null,
    role = "백엔드",
    displayOrder = 0.0,
    status = ExperienceProjectStatus.ACTIVE,
)
