package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceProjectFixture
import com.jobdori.core.domain.experience.error.ExperienceProjectNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk

class ExperienceProjectReaderTest : StringSpec({

    val experienceProjectRepository = mockk<ExperienceProjectRepository>()
    val experienceProjectReader = ExperienceProjectReader(experienceProjectRepository)

    beforeTest {
        clearMocks(experienceProjectRepository)
    }

    "경험 프로젝트를 조회한다" {
        // given
        val project = ExperienceProjectFixture.create(id = 1L, workspaceId = 10L)
        every { experienceProjectRepository.findByIdAndWorkspaceId(1L, 10L) } returns project

        // when & then
        experienceProjectReader.getProject(workspaceId = 10L, projectId = 1L) shouldBe project
    }

    "경험 프로젝트가 없으면 예외를 던진다" {
        // given
        every { experienceProjectRepository.findByIdAndWorkspaceId(1L, 10L) } returns null

        // when & then
        shouldThrow<ExperienceProjectNotFoundException> {
            experienceProjectReader.getProject(workspaceId = 10L, projectId = 1L)
        }
    }

    "여러 경험 프로젝트를 ID 기준 Map으로 조회한다" {
        // given
        val projects = listOf(
            ExperienceProjectFixture.create(id = 1L, workspaceId = 10L),
            ExperienceProjectFixture.create(id = 2L, workspaceId = 10L),
        )
        every {
            experienceProjectRepository.findAllByIdsAndWorkspaceId(setOf(1L, 2L), 10L)
        } returns projects

        // when
        val result = experienceProjectReader.getProjects(workspaceId = 10L, projectIds = listOf(1L, 2L, 1L))

        // then
        result shouldContainExactly mapOf(1L to projects[0], 2L to projects[1])
    }

    "여러 경험 프로젝트 조회 시 요청 ID가 비어 있으면 빈 Map을 반환한다" {
        // when & then
        experienceProjectReader.getProjects(workspaceId = 10L, projectIds = emptyList()) shouldContainExactly emptyMap()
    }

    "경험 프로젝트 목록을 slice로 조회하고 다음 cursor를 반환한다" {
        // given
        val projects = listOf(
            ExperienceProjectFixture.create(id = 3L, workspaceId = 10L),
            ExperienceProjectFixture.create(id = 2L, workspaceId = 10L),
            ExperienceProjectFixture.create(id = 1L, workspaceId = 10L),
        )
        every {
            experienceProjectRepository.findAllByWorkspaceId(
                workspaceId = 10L,
                cursorId = 4L,
                size = 3,
            )
        } returns projects

        // when
        val result = experienceProjectReader.getProjects(workspaceId = 10L, cursor = "4", size = 2)

        // then
        result.items shouldContainExactly projects.take(2)
        result.nextCursor shouldBe "2"
    }

    "경험 프로젝트 목록이 마지막 slice면 다음 cursor를 반환하지 않는다" {
        // given
        val projects = listOf(ExperienceProjectFixture.create(id = 1L, workspaceId = 10L))
        every {
            experienceProjectRepository.findAllByWorkspaceId(
                workspaceId = 10L,
                cursorId = null,
                size = 3,
            )
        } returns projects

        // when
        val result = experienceProjectReader.getProjects(workspaceId = 10L, cursor = "invalid", size = 2)

        // then
        result.items shouldContainExactly projects
        result.nextCursor.shouldBeNull()
    }

})
