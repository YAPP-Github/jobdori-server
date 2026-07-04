package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceFixture
import com.jobdori.core.domain.experience.error.ExperienceNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ExperienceReaderTest : StringSpec({

    val experienceRepository = mockk<ExperienceRepository>()
    val experienceReader = ExperienceReader(
        experienceRepository = experienceRepository,
    )

    "경험을 조회한다" {
        // given
        val experience = ExperienceFixture.create(id = 1L, workspaceId = 10L)
        every { experienceRepository.findByIdAndWorkspaceId(1L, 10L) } returns experience

        // when & then
        experienceReader.getExperience(workspaceId = 10L, experienceId = 1L) shouldBe experience
    }

    "경험이 없으면 예외를 던진다" {
        // given
        every { experienceRepository.findByIdAndWorkspaceId(1L, 10L) } returns null

        // when & then
        shouldThrow<ExperienceNotFoundException> {
            experienceReader.getExperience(workspaceId = 10L, experienceId = 1L)
        }
    }

    "워크스페이스의 경험 목록을 slice로 조회한다" {
        // given
        val experiences = listOf(
            ExperienceFixture.create(id = 3L, workspaceId = 10L),
            ExperienceFixture.create(id = 2L, workspaceId = 10L),
            ExperienceFixture.create(id = 1L, workspaceId = 10L),
        )
        every {
            experienceRepository.findAllByWorkspaceId(
                workspaceId = 10L,
                cursorId = 4L,
                size = 3,
            )
        } returns experiences

        // when
        val result = experienceReader.getExperiences(
            workspaceId = 10L,
            projectId = null,
            cursor = "4",
            size = 2,
        )

        // then
        result.items shouldContainExactly experiences.take(2)
        result.nextCursor shouldBe "2"
    }

    "프로젝트 경험 목록을 slice로 조회한다" {
        // given
        val experiences = listOf(
            ExperienceFixture.create(id = 3L, workspaceId = 10L, projectId = 5L),
            ExperienceFixture.create(id = 2L, workspaceId = 10L, projectId = 5L),
            ExperienceFixture.create(id = 1L, workspaceId = 10L, projectId = 5L),
        )
        every {
            experienceRepository.findAllByWorkspaceIdAndProjectId(
                workspaceId = 10L,
                projectId = 5L,
                cursorId = 4L,
                size = 3,
            )
        } returns experiences

        // when
        val result = experienceReader.getExperiences(
            workspaceId = 10L,
            projectId = 5L,
            cursor = "4",
            size = 2,
        )

        // then
        result.items shouldContainExactly experiences.take(2)
        result.nextCursor shouldBe "2"
    }

    "검색어가 포함된 경험 목록을 slice로 조회한다" {
        // given
        val experiences = listOf(
            ExperienceFixture.create(id = 3L, workspaceId = 10L, title = "Kotlin 성능 개선"),
            ExperienceFixture.create(id = 2L, workspaceId = 10L, title = "검색 API 개선"),
            ExperienceFixture.create(id = 1L, workspaceId = 10L, title = "응답 포맷 개선"),
        )
        every {
            experienceRepository.searchAllByWorkspaceId(
                workspaceId = 10L,
                keyword = "개선",
                cursorId = 4L,
                size = 3,
            )
        } returns experiences

        // when
        val result = experienceReader.searchExperiences(
            workspaceId = 10L,
            keyword = "개선",
            cursor = "4",
            size = 2,
        )

        // then
        result.items shouldContainExactly experiences.take(2)
        result.nextCursor shouldBe "2"
    }

})
