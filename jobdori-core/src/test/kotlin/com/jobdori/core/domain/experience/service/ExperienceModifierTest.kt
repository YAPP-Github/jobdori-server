package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceFixture
import com.jobdori.core.domain.experience.error.ExperienceNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ExperienceModifierTest : StringSpec({

    val experienceRepository = mockk<ExperienceRepository>()
    val experienceModifier = ExperienceModifier(
        experienceRepository = experienceRepository,
    )

    "경험을 수정한다" {
        // given
        val experience = ExperienceFixture.create(id = 1L, workspaceId = 10L, projectId = 5L)
        val contents = ExperienceContents.star(
            situation = "상황",
            task = "과제",
            action = "행동",
            result = "결과",
        )
        every { experienceRepository.save(any()) } answers { firstArg() }
        every { experienceRepository.findByIdAndWorkspaceId(1L, 10L) } returns experience

        // when
        val modified = experienceModifier.modify(
            workspaceId = 10L,
            experienceId = 1L,
            projectId = 6L,
            tags = listOf("GraphQL"),
            title = "GraphQL API 개선",
            contents = contents,
        )

        // then
        modified shouldBe experience.copy(
            projectId = 6L,
            tags = listOf("GraphQL"),
            title = "GraphQL API 개선",
            contents = contents,
        )
    }

    "수정 값이 null이면 기존 값을 유지한다" {
        // given
        val experience = ExperienceFixture.create(id = 1L, workspaceId = 10L, projectId = 5L)
        every { experienceRepository.findByIdAndWorkspaceId(1L, 10L) } returns experience
        every { experienceRepository.save(any()) } answers { firstArg() }

        // when
        val modified = experienceModifier.modify(
            workspaceId = 10L,
            experienceId = 1L,
            projectId = null,
            tags = null,
            title = null,
            contents = null,
        )

        // then
        modified shouldBe experience
    }

    "수정할 경험이 없으면 예외를 던진다" {
        // given
        every { experienceRepository.findByIdAndWorkspaceId(1L, 10L) } returns null

        // when & then
        shouldThrow<ExperienceNotFoundException> {
            experienceModifier.modify(
                workspaceId = 10L,
                experienceId = 1L,
                projectId = 6L,
                tags = listOf("GraphQL"),
                title = "GraphQL API 개선",
                contents = ExperienceContents.free("수정 내용"),
            )
        }
    }

})
