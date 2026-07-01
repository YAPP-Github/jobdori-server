package com.jobdori.core.domain.experience.service

import com.jobdori.common.model.Period
import com.jobdori.core.domain.experience.ExperienceProjectStatus
import com.jobdori.core.domain.experience.repository.ExperienceProjectRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.math.BigDecimal
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
            name = "채용 서비스",
            summary = "채용 서비스 백엔드 개발",
            period = period,
            role = "백엔드 개발자",
        )

        // then
        project.id shouldBe 0L
        project.workspaceId shouldBe 1L
        project.name shouldBe "채용 서비스"
        project.summary shouldBe "채용 서비스 백엔드 개발"
        project.period shouldBe period
        project.role shouldBe "백엔드 개발자"
        project.displayOrder shouldBe BigDecimal.ZERO
        project.status shouldBe ExperienceProjectStatus.ACTIVE
    }

})
