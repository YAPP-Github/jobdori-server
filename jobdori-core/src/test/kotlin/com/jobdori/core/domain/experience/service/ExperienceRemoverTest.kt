package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceFixture
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.error.ExperienceNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class ExperienceRemoverTest : StringSpec({

    val experienceRepository = mockk<ExperienceRepository>()
    val experienceRemover = ExperienceRemover(
        experienceRepository = experienceRepository,
    )

    "경험 상태를 삭제로 변경한다" {
        // given
        val experience = ExperienceFixture.create(id = 1L, workspaceId = 10L)
        val savedExperience = slot<Experience>()
        every { experienceRepository.findByIdAndWorkspaceId(1L, 10L) } returns experience
        every { experienceRepository.save(capture(savedExperience)) } answers { firstArg() }

        // when
        experienceRemover.remove(workspaceId = 10L, experienceId = 1L)

        // then
        savedExperience.captured shouldBe experience.copy(status = ExperienceStatus.DELETED)
        verify(exactly = 1) { experienceRepository.save(any()) }
    }

    "삭제할 경험이 없으면 예외를 던진다" {
        // given
        every { experienceRepository.findByIdAndWorkspaceId(1L, 10L) } returns null

        // when & then
        shouldThrow<ExperienceNotFoundException> {
            experienceRemover.remove(workspaceId = 10L, experienceId = 1L)
        }
    }

})
