package com.jobdori.core.domain.experience.service

import com.jobdori.common.model.Period
import com.jobdori.core.domain.experience.ExperienceProjectFixture
import com.jobdori.core.domain.experience.error.ExperienceProjectNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

class ExperienceProjectModifierTest : StringSpec({

    val experienceProjectRepository = mockk<ExperienceProjectRepository>()
    val experienceProjectModifier = ExperienceProjectModifier(
        experienceProjectRepository = experienceProjectRepository,
    )

    "경험 프로젝트를 수정한다" {
        // given
        val project = ExperienceProjectFixture.create(
            id = 1L,
            workspaceId = 10L,
            name = "기존 프로젝트",
            summary = "기존 요약",
            role = "기존 역할",
        )
        val period = Period(
            startAt = LocalDate.of(2025, 1, 1),
            endAt = LocalDate.of(2025, 6, 30),
        )
        every { experienceProjectRepository.findByIdAndWorkspaceId(1L, 10L) } returns project
        every { experienceProjectRepository.save(any()) } answers { firstArg() }

        // when
        val modified = experienceProjectModifier.modify(
            workspaceId = 10L,
            projectId = 1L,
            name = "수정 프로젝트",
            summary = "수정 요약",
            period = period,
            role = "수정 역할",
        )

        // then
        modified shouldBe project.copy(
            name = "수정 프로젝트",
            summary = "수정 요약",
            period = period,
            role = "수정 역할",
        )
    }

    "nullable 수정 값이 null이면 기존 값을 제거한다" {
        // given
        val project = ExperienceProjectFixture.create(id = 1L, workspaceId = 10L, role = "기존 역할")
        every { experienceProjectRepository.findByIdAndWorkspaceId(1L, 10L) } returns project
        every { experienceProjectRepository.save(any()) } answers { firstArg() }

        // when
        val modified = experienceProjectModifier.modify(
            workspaceId = 10L,
            projectId = 1L,
            name = "수정 프로젝트",
            summary = "수정 요약",
            period = null,
            role = null,
        )

        // then
        modified shouldBe project.copy(
            name = "수정 프로젝트",
            summary = "수정 요약",
            period = null,
            role = null,
        )
    }

    "수정할 경험 프로젝트가 없으면 예외를 던진다" {
        // given
        every { experienceProjectRepository.findByIdAndWorkspaceId(1L, 10L) } returns null

        // when & then
        shouldThrow<ExperienceProjectNotFoundException> {
            experienceProjectModifier.modify(
                workspaceId = 10L,
                projectId = 1L,
                name = "수정 프로젝트",
                summary = "수정 요약",
                period = null,
                role = "수정 역할",
            )
        }
    }

})
