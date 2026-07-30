package com.jobdori.api.application.experience.service

import com.jobdori.api.application.experience.dto.request.CreateExperienceRequest
import com.jobdori.api.application.experience.dto.request.UpdateExperienceRequest
import com.jobdori.api.application.experience.dto.request.contents.ExperienceContentsRequest
import com.jobdori.api.application.experience.dto.request.contents.FreeExperienceContentsRequest
import com.jobdori.api.application.workspace.service.WorkspaceAccessValidationService
import com.jobdori.common.model.Period
import com.jobdori.common.model.SliceResult
import com.jobdori.core.application.experience.ExperienceContentsPolishService
import com.jobdori.core.application.experience.PolishedExperience
import com.jobdori.core.application.experiencerecommendation.GetExperienceRecommendationService
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceContentsType
import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.StarExperienceContents
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
import java.time.LocalDate

class ExperienceServiceTest : StringSpec({

    val experienceCreator = mockk<ExperienceCreator>()
    val experienceReader = mockk<ExperienceReader>()
    val experienceModifier = mockk<ExperienceModifier>()
    val experienceRemover = mockk<ExperienceRemover>()
    val experienceProjectReader = mockk<ExperienceProjectReader>()
    val workspaceAccessValidationService = mockk<WorkspaceAccessValidationService>()
    val getExperienceRecommendationService = mockk<GetExperienceRecommendationService>()
    val experienceContentsPolishService = mockk<ExperienceContentsPolishService>()
    val experienceService = ExperienceService(
        workspaceAccessValidationService = workspaceAccessValidationService,
        experienceCreator = experienceCreator,
        experienceReader = experienceReader,
        experienceModifier = experienceModifier,
        experienceRemover = experienceRemover,
        experienceProjectReader = experienceProjectReader,
        getExperienceRecommendationService = getExperienceRecommendationService,
        experienceContentsPolishService = experienceContentsPolishService,
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
        val projectPeriod = Period(
            startAt = LocalDate.of(2025, 1, 1),
            endAt = LocalDate.of(2025, 6, 30),
        )
        val project = project(id = 3L, period = projectPeriod, role = "백엔드 개발")
        val contents = StarExperienceContents("상황", "과제", "행동", "결과")
        val request = CreateExperienceRequest(
            projectId = 3L,
            tags = listOf("Kotlin"),
            title = "경험",
            contents = freeContentsRequest("경험 내용"),
        )
        val experience = experience(id = 100L, projectId = 3L, contents = contents)
        every { experienceProjectReader.getProject(workspaceId = 1L, projectId = 3L) } returns project
        every {
            experienceContentsPolishService.polishFreeStyleToStar("경험 내용")
        } returns PolishedExperience(null, null, null, emptyList(), contents)
        every {
            experienceCreator.create(
                workspaceId = 1L,
                projectId = 3L,
                command = ExperienceCreateCommand(
                    tags = listOf("Kotlin"),
                    title = "경험",
                    contents = contents,
                    period = projectPeriod,
                    role = null,
                ),
            )
        } returns experience

        // when
        val response = experienceService.createExperience(
            userId = 10L,
            workspaceId = "workspace-id",
            projectId = 3L,
            request = request,
        )

        // then
        response.experienceId shouldBe 100L
        response.project?.projectId shouldBe 3L
        response.title shouldBe "경험"
        response.contents.type shouldBe ExperienceContentsType.STAR
        verify(exactly = 1) { experienceContentsPolishService.polishFreeStyleToStar("경험 내용") }
        verify(exactly = 1) { experienceProjectReader.getProject(workspaceId = 1L, projectId = 3L) }
    }

    "FREE 내용에서 추출한 제목과 기간, 태그를 비어 있는 생성 필드에 사용한다" {
        val extractedPeriod = Period(
            startAt = LocalDate.of(2024, 3, 1),
            endAt = LocalDate.of(2024, 8, 31),
        )
        val project = project(id = 3L, role = "프로젝트 역할")
        val contents = StarExperienceContents("상황", "과제", "행동", "결과")
        val request = CreateExperienceRequest(
            projectId = 3L,
            title = "",
            contents = freeContentsRequest("2024년 3월부터 8월까지 백엔드 리드로 성능을 개선했다"),
        )
        val created = experience(id = 100L, projectId = 3L, title = "성능 개선", contents = contents)
        every { experienceProjectReader.getProject(1L, 3L) } returns project
        every {
            experienceContentsPolishService.polishFreeStyleToStar(any())
        } returns PolishedExperience(
            title = "성능 개선",
            period = extractedPeriod,
            role = "백엔드 리드",
            tags = listOf("성능 개선", "리더십"),
            contents = contents,
        )
        every {
            experienceCreator.create(
                workspaceId = 1L,
                projectId = 3L,
                command = ExperienceCreateCommand(
                    tags = listOf("성능 개선", "리더십"),
                    title = "성능 개선",
                    contents = contents,
                    period = extractedPeriod,
                    role = null,
                ),
            )
        } returns created

        experienceService.createExperience(10L, "workspace-id", 3L, request)

        verify(exactly = 1) {
            experienceCreator.create(
                workspaceId = 1L,
                projectId = 3L,
                command = ExperienceCreateCommand(
                    tags = listOf("성능 개선", "리더십"),
                    title = "성능 개선",
                    contents = contents,
                    period = extractedPeriod,
                    role = null,
                ),
            )
        }
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
        val contents = StarExperienceContents("수정 상황", "수정 과제", "수정 행동", "수정 결과")
        val request = UpdateExperienceRequest(
            projectId = 5L,
            tags = listOf("Spring"),
            title = "수정 경험",
            contents = freeContentsRequest("수정 내용"),
            role = null,
            period = null,
        )
        val modified = experience(id = 1L, projectId = 5L, title = "수정 경험", contents = contents)
        every { experienceProjectReader.getProject(workspaceId = 1L, projectId = 5L) } returns project(id = 5L)
        every {
            experienceContentsPolishService.polishFreeStyleToStar("수정 내용")
        } returns PolishedExperience(null, null, null, emptyList(), contents)
        every {
            experienceModifier.modify(
                workspaceId = 1L,
                experienceId = 1L,
                projectId = 5L,
                tags = listOf("Spring"),
                title = "수정 경험",
                contents = contents,
                period = null,
                role = null,
            )
        } returns modified

        // when
        val response = experienceService.modifyExperience(
            userId = 10L,
            workspaceId = "workspace-id",
            experienceId = 1L,
            request = request,
        )

        // then
        response.project?.projectId shouldBe 5L
        response.title shouldBe "수정 경험"
        response.contents.type shouldBe ExperienceContentsType.STAR
        verify(exactly = 1) { experienceContentsPolishService.polishFreeStyleToStar("수정 내용") }
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
    displayOrder = 0.0,
    status = ExperienceStatus.ACTIVE,
)

private fun freeContentsRequest(content: String) = ExperienceContentsRequest(
    type = ExperienceContentsType.FREE,
    free = FreeExperienceContentsRequest(content = content),
)

private fun project(
    id: Long,
    period: Period? = null,
    role: String? = "백엔드 개발자",
) = ExperienceProject(
    id = id,
    workspaceId = 1L,
    name = "프로젝트 $id",
    summary = "프로젝트 요약",
    period = period,
    role = role,
    displayOrder = 0.0,
    status = ExperienceProjectStatus.ACTIVE,
)
