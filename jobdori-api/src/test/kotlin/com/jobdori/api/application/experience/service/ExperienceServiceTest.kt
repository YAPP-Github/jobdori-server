package com.jobdori.api.application.experience.service

import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.model.SliceResult
import com.jobdori.core.application.experiencerecommendation.GetExperienceRecommendationService
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.service.ExperienceCreator
import com.jobdori.core.domain.experience.service.ExperienceModifier
import com.jobdori.core.domain.experience.service.ExperienceProjectReader
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experience.service.ExperienceRemover
import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import com.jobdori.core.domain.workspace.Workspace
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal

class ExperienceServiceTest : StringSpec({

    val experienceCreator = mockk<ExperienceCreator>()
    val experienceReader = mockk<ExperienceReader>()
    val experienceModifier = mockk<ExperienceModifier>()
    val experienceRemover = mockk<ExperienceRemover>()
    val experienceProjectReader = mockk<ExperienceProjectReader>()
    val workspaceAccessValidationService = mockk<WorkspaceAccessValidationService>()
    val getExperienceRecommendationService = mockk<GetExperienceRecommendationService>()
    val experienceService = ExperienceService(
        workspaceAccessValidationService = workspaceAccessValidationService,
        experienceCreator = experienceCreator,
        experienceReader = experienceReader,
        experienceModifier = experienceModifier,
        experienceRemover = experienceRemover,
        experienceProjectReader = experienceProjectReader,
        getExperienceRecommendationService = getExperienceRecommendationService,
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

    "프로젝트를 확인한 뒤 경험을 생성하고 응답을 반환한다" {
        // given
        val project = project(id = 3L)
        val contents = ExperienceContents.free("경험 내용")
        val experience = experience(id = 100L, projectId = 3L, contents = contents)
        every { experienceProjectReader.getProject(workspaceId = 1L, projectId = 3L) } returns project
        every {
            experienceCreator.create(
                workspaceId = 1L,
                projectId = 3L,
                command = ExperienceCreateCommand(
                    tags = listOf("Kotlin"),
                    title = "경험",
                    contents = contents,
                ),
            )
        } returns experience

        // when
        val response = experienceService.createExperience(
            userId = 10L,
            workspaceId = "workspace-id",
            projectId = 3L,
            tags = listOf("Kotlin"),
            title = "경험",
            contents = contents,
        )

        // then
        response.experienceId shouldBe 100L
        response.project?.projectId shouldBe 3L
        response.title shouldBe "경험"
        verify(exactly = 1) { experienceProjectReader.getProject(workspaceId = 1L, projectId = 3L) }
    }

    "경험 목록에서 project를 요청하면 프로젝트를 한 번에 조회해 응답에 연결한다" {
        // given
        val experiences = listOf(
            experience(id = 2L, projectId = 3L),
            experience(id = 1L, projectId = 4L),
        )
        every {
            experienceReader.getExperiences(
                workspaceId = 1L,
                projectId = null,
                cursor = null,
                size = 2,
            )
        } returns SliceResult(items = experiences, nextCursor = "1")
        every {
            experienceProjectReader.getProjects(
                workspaceId = 1L,
                projectIds = listOf(3L, 4L),
            )
        } returns mapOf(3L to project(id = 3L), 4L to project(id = 4L))

        // when
        val response = experienceService.getExperiences(
            userId = 10L,
            workspaceId = "workspace-id",
            projectId = null,
            cursor = null,
            size = 2,
            includeProjects = true,
        )

        // then
        response.experiences.map { it.project?.projectId } shouldContainExactly listOf(3L, 4L)
        response.cursor.nextCursor shouldBe "1"
    }

    "경험 목록에서 project를 요청하지 않으면 프로젝트를 조회하지 않는다" {
        // given
        val experiences = listOf(experience(id = 1L, projectId = 3L))
        every {
            experienceReader.getExperiences(
                workspaceId = 1L,
                projectId = null,
                cursor = null,
                size = 2,
            )
        } returns SliceResult(items = experiences, nextCursor = null)

        // when
        val response = experienceService.getExperiences(
            userId = 10L,
            workspaceId = "workspace-id",
            projectId = null,
            cursor = null,
            size = 2,
            includeProjects = false,
        )

        // then
        response.experiences.single().project.shouldBeNull()
        verify(exactly = 0) { experienceProjectReader.getProjects(any(), any<Collection<Long>>()) }
    }

    "경험 검색에서 project를 요청하면 프로젝트를 한 번에 조회해 응답에 연결한다" {
        // given
        val experiences = listOf(
            experience(id = 2L, projectId = 3L, title = "Kotlin 성능 개선"),
            experience(id = 1L, projectId = 4L, title = "검색 API 개선"),
        )
        every {
            experienceReader.searchExperiences(
                workspaceId = 1L,
                keyword = "개선",
                cursor = null,
                size = 2,
            )
        } returns SliceResult(items = experiences, nextCursor = "1")
        every {
            experienceProjectReader.getProjects(
                workspaceId = 1L,
                projectIds = listOf(3L, 4L),
            )
        } returns mapOf(3L to project(id = 3L), 4L to project(id = 4L))

        // when
        val response = experienceService.searchExperiences(
            userId = 10L,
            workspaceId = "workspace-id",
            keyword = "개선",
            cursor = null,
            size = 2,
            includeProjects = true,
        )

        // then
        response.experiences.map { it.project?.projectId } shouldContainExactly listOf(3L, 4L)
        response.cursor.nextCursor shouldBe "1"
    }

    "경험 검색에서 project를 요청하지 않으면 프로젝트를 조회하지 않는다" {
        // given
        val experiences = listOf(experience(id = 1L, projectId = 3L, title = "Kotlin 성능 개선"))
        every {
            experienceReader.searchExperiences(
                workspaceId = 1L,
                keyword = "Kotlin",
                cursor = null,
                size = 2,
            )
        } returns SliceResult(items = experiences, nextCursor = null)

        // when
        val response = experienceService.searchExperiences(
            userId = 10L,
            workspaceId = "workspace-id",
            keyword = "Kotlin",
            cursor = null,
            size = 2,
            includeProjects = false,
        )

        // then
        response.experiences.single().project.shouldBeNull()
        verify(exactly = 0) { experienceProjectReader.getProjects(any(), any<Collection<Long>>()) }
    }

    "경험 수정 시 변경 대상 프로젝트를 먼저 확인하고 수정 결과의 프로젝트를 응답에 연결한다" {
        // given
        val contents = ExperienceContents.free("수정 내용")
        val modified = experience(id = 1L, projectId = 5L, title = "수정 경험", contents = contents)
        every { experienceProjectReader.getProject(workspaceId = 1L, projectId = 5L) } returns project(id = 5L)
        every {
            experienceModifier.modify(
                workspaceId = 1L,
                experienceId = 1L,
                projectId = 5L,
                tags = listOf("Spring"),
                title = "수정 경험",
                contents = contents,
            )
        } returns modified

        // when
        val response = experienceService.modifyExperience(
            userId = 10L,
            workspaceId = "workspace-id",
            experienceId = 1L,
            projectId = 5L,
            tags = listOf("Spring"),
            title = "수정 경험",
            contents = contents,
        )

        // then
        response.project?.projectId shouldBe 5L
        response.title shouldBe "수정 경험"
        verify(exactly = 2) { experienceProjectReader.getProject(workspaceId = 1L, projectId = 5L) }
    }

    "경험 삭제는 remover에 위임한다" {
        // given
        every { experienceRemover.remove(workspaceId = 1L, experienceId = 1L) } returns Unit

        // when
        experienceService.removeExperience(userId = 10L, workspaceId = "workspace-id", experienceId = 1L)

        // then
        verify(exactly = 1) { experienceRemover.remove(workspaceId = 1L, experienceId = 1L) }
    }

})

private fun experience(
    id: Long,
    projectId: Long,
    title: String = "경험",
    contents: ExperienceContents = ExperienceContents.free("경험 내용"),
) = Experience(
    id = id,
    workspaceId = 1L,
    projectId = projectId,
    tags = listOf("Kotlin"),
    title = title,
    contents = contents,
    displayOrder = BigDecimal.ZERO,
    status = ExperienceStatus.ACTIVE,
)

private fun project(id: Long) = ExperienceProject(
    id = id,
    workspaceId = 1L,
    name = "프로젝트 $id",
    summary = "프로젝트 요약",
    period = null,
    role = "백엔드 개발자",
    displayOrder = BigDecimal.ZERO,
    status = ExperienceProjectStatus.ACTIVE,
)
