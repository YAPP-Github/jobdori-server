package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceProject
import com.jobdori.core.domain.experience.ExperienceProjectFixture
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.error.ExperienceProjectNotFoundException
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class ExperienceProjectRemoverTest : StringSpec({

    val experienceProjectRepository = mockk<ExperienceProjectRepository>()
    val experienceRepository = mockk<ExperienceRepository>()
    val experienceProjectRemover = ExperienceProjectRemover(
        experienceProjectRepository = experienceProjectRepository,
        experienceRepository = experienceRepository,
    )

    "경험 프로젝트 상태를 삭제로 변경한다" {
        // given
        val project = ExperienceProjectFixture.create(id = 1L, workspaceId = 10L)
        val savedProject = slot<ExperienceProject>()
        every { experienceProjectRepository.findByIdAndWorkspaceId(1L, 10L) } returns project
        every { experienceProjectRepository.save(capture(savedProject)) } answers { firstArg() }
        every {
            experienceRepository.updateStatusByWorkspaceIdAndProjectId(
                10L,
                1L,
                ExperienceStatus.DELETED,
            )
        } returns Unit

        // when
        experienceProjectRemover.remove(workspaceId = 10L, projectId = 1L)

        // then
        savedProject.captured shouldBe project.copy(status = ExperienceProjectStatus.DELETED)
        verify(exactly = 1) { experienceProjectRepository.save(any()) }
        verify(exactly = 1) {
            experienceRepository.updateStatusByWorkspaceIdAndProjectId(
                workspaceId = 10L,
                projectId = 1L,
                status = ExperienceStatus.DELETED,
            )
        }
    }

    "삭제할 경험 프로젝트가 없으면 예외를 던진다" {
        // given
        every { experienceProjectRepository.findByIdAndWorkspaceId(1L, 10L) } returns null

        // when & then
        shouldThrow<ExperienceProjectNotFoundException> {
            experienceProjectRemover.remove(workspaceId = 10L, projectId = 1L)
        }
        verify(exactly = 0) {
            experienceRepository.updateStatusByWorkspaceIdAndProjectId(any(), any(), any())
        }
    }

})
