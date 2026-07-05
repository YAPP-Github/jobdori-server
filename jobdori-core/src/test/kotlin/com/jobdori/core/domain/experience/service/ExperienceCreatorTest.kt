package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import com.jobdori.core.domain.experience.service.command.ExperienceCreateCommand
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.math.BigDecimal

class ExperienceCreatorTest : StringSpec({

    val experienceRepository = mockk<ExperienceRepository>()
    val experienceCreator = ExperienceCreator(
        experienceRepository = experienceRepository,
    )

    "경험을 생성한다" {
        // given
        val contents = ExperienceContents.free("성과 중심 경험")
        every { experienceRepository.save(any()) } answers { firstArg() }

        // when
        val experience = experienceCreator.create(
            workspaceId = 10L,
            projectId = 1L,
            command = ExperienceCreateCommand(
                tags = listOf("Kotlin", "Spring"),
                title = "검색 성능 개선",
                contents = contents,
            ),
        )

        // then
        experience.id shouldBe 0L
        experience.workspaceId shouldBe 10L
        experience.projectId shouldBe 1L
        experience.tags shouldBe listOf("Kotlin", "Spring")
        experience.title shouldBe "검색 성능 개선"
        experience.contents shouldBe contents
        experience.displayOrder shouldBe BigDecimal.ZERO
        experience.status shouldBe ExperienceStatus.ACTIVE
    }

    "경험 목록을 생성한다" {
        // given
        every { experienceRepository.saveAll(any()) } answers { firstArg() }

        // when
        val experiences = experienceCreator.create(
            workspaceId = 10L,
            projectId = 1L,
            commands = listOf(
                ExperienceCreateCommand(
                    tags = listOf("Kotlin", "Spring"),
                    title = "검색 성능 개선",
                    contents = ExperienceContents.free("검색 성능을 개선했다"),
                ),
                ExperienceCreateCommand(
                    tags = listOf("PostgreSQL"),
                    title = "데이터 모델 개선",
                    contents = ExperienceContents.free("데이터 모델을 개선했다"),
                ),
            ),
        )

        // then
        experiences.map { it.workspaceId } shouldContainExactly listOf(10L, 10L)
        experiences.map { it.projectId } shouldContainExactly listOf(1L, 1L)
        experiences.map { it.title } shouldContainExactly listOf("검색 성능 개선", "데이터 모델 개선")
        experiences.map { it.displayOrder } shouldContainExactly listOf(BigDecimal.ZERO, BigDecimal.ZERO)
        experiences.map { it.status } shouldContainExactly listOf(ExperienceStatus.ACTIVE, ExperienceStatus.ACTIVE)
        verify(exactly = 1) { experienceRepository.saveAll(any()) }
        verify(exactly = 0) { experienceRepository.save(any()) }
    }

})
