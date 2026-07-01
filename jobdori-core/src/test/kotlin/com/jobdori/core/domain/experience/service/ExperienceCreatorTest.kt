package com.jobdori.core.domain.experience.service

import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.experience.repository.ExperienceRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
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
            tags = listOf("Kotlin", "Spring"),
            title = "검색 성능 개선",
            contents = contents,
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

})
