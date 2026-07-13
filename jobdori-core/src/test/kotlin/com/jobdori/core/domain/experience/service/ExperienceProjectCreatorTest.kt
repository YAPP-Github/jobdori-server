package com.jobdori.core.domain.experience.service

import com.jobdori.common.model.Period
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import com.jobdori.core.domain.experience.service.command.ExperienceProjectCreateCommand
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate

class ExperienceProjectCreatorTest : StringSpec({

    val experienceProjectRepository = mockk<ExperienceProjectRepository>()
    val experienceProjectCreator = ExperienceProjectCreator(experienceProjectRepository)

    "경험 프로젝트를 생성한다" {
        // given
        val period = Period(
            startAt = LocalDate.of(2024, 1, 1),
            endAt = LocalDate.of(2024, 12, 31),
        )
        every { experienceProjectRepository.save(any()) } answers { firstArg() }

        // when
        val project = experienceProjectCreator.create(
            workspaceId = 1L,
            command = ExperienceProjectCreateCommand(
                name = "채용 서비스",
                summary = "채용 서비스 백엔드 개발",
                period = period,
                role = "백엔드 개발자",
            ),
        )

        // then
        project.id shouldBe 0L
        project.workspaceId shouldBe 1L
        project.name shouldBe "채용 서비스"
        project.summary shouldBe "채용 서비스 백엔드 개발"
        project.period shouldBe period
        project.role shouldBe "백엔드 개발자"
        project.displayOrder shouldBe 0.0
        project.status shouldBe ExperienceProjectStatus.ACTIVE
    }

    "경험 프로젝트 목록을 생성한다" {
        // given
        val period = Period(
            startAt = LocalDate.of(2024, 1, 1),
            endAt = LocalDate.of(2024, 12, 31),
        )
        every { experienceProjectRepository.saveAll(any()) } answers { firstArg() }

        // when
        val projects = experienceProjectCreator.create(
            workspaceId = 1L,
            commands = listOf(
                ExperienceProjectCreateCommand(
                    name = "채용 서비스",
                    summary = "채용 서비스 백엔드 개발",
                    period = period,
                    role = "백엔드 개발자",
                ),
                ExperienceProjectCreateCommand(
                    name = "이력서 서비스",
                    summary = "이력서 서비스 백엔드 개발",
                    period = null,
                    role = null,
                ),
            ),
        )

        // then
        projects.map { it.workspaceId } shouldContainExactly listOf(1L, 1L)
        projects.map { it.name } shouldContainExactly listOf("채용 서비스", "이력서 서비스")
        projects.map { it.summary } shouldContainExactly listOf("채용 서비스 백엔드 개발", "이력서 서비스 백엔드 개발")
        projects.map { it.period } shouldContainExactly listOf(period, null)
        projects.map { it.role } shouldContainExactly listOf("백엔드 개발자", null)
        projects.map { it.displayOrder } shouldContainExactly listOf(0.0, 0.0)
        projects.map { it.status } shouldContainExactly listOf(
            ExperienceProjectStatus.ACTIVE,
            ExperienceProjectStatus.ACTIVE,
        )
        verify(exactly = 1) { experienceProjectRepository.saveAll(any()) }
        verify(exactly = 0) { experienceProjectRepository.save(any()) }
    }

})
