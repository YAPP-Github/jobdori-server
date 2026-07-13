package com.jobdori.core.application.experiencerecommendation

import com.jobdori.core.domain.experience.ExperienceFixture
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experiencerecommendation.JdExperienceRecommendation
import com.jobdori.core.domain.experiencerecommendation.RecommendedExperience
import com.jobdori.core.domain.experiencerecommendation.repository.JdExperienceRecommendationRepository
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdStatus
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class GetExperienceRecommendationServiceTest : StringSpec({

    val jdRepository = mockk<JdRepository>()
    val experienceReader = mockk<ExperienceReader>()
    val recommendationRepository = mockk<JdExperienceRecommendationRepository>()
    val generateService = mockk<GenerateExperienceRecommendationService>()
    val service = GetExperienceRecommendationService(
        jdRepository = jdRepository,
        experienceReader = experienceReader,
        recommendationRepository = recommendationRepository,
        generateService = generateService,
    )

    val jd = jd(id = 100L, publicId = "jd-pub-1", workspaceId = 1L)

    "등록되지 않은 JD를 조회하면 예외를 던진다" {
        // given
        every { jdRepository.findByPublicIdAndWorkspaceId("jd-pub-1", 1L) } returns null

        // when & then
        shouldThrow<JdNotFoundException> {
            service.getOrRefresh(1L, "jd-pub-1")
        }
        verify(exactly = 0) { experienceReader.signature(any()) }
    }

    "경험 세트 시그니처가 캐시와 같으면 재생성 없이 캐시를 그대로 반환한다" {
        // given
        val cached = JdExperienceRecommendation.newInstance(
            jdId = 100L,
            items = listOf(RecommendedExperience(experienceId = 1L, matchRate = 80, reason = "이유")),
            sourceSignature = "2:2026-01-01T00:00",
        )
        every { jdRepository.findByPublicIdAndWorkspaceId("jd-pub-1", 1L) } returns jd
        every { experienceReader.signature(1L) } returns "2:2026-01-01T00:00"
        every { recommendationRepository.findByJdId(100L) } returns cached

        // when
        val result = service.getOrRefresh(1L, "jd-pub-1")

        // then
        result shouldBe cached
        verify(exactly = 0) { experienceReader.findAllActive(any()) }
        verify(exactly = 0) { generateService.generate(any(), any()) }
        verify(exactly = 0) { recommendationRepository.upsert(any()) }
    }

    "캐시가 없으면 경험 목록으로 추천을 생성하고 저장한다" {
        // given
        val experiences = listOf(ExperienceFixture.create(id = 1L, workspaceId = 1L))
        val generated = listOf(RecommendedExperience(experienceId = 1L, matchRate = 90, reason = "이유"))
        every { jdRepository.findByPublicIdAndWorkspaceId("jd-pub-1", 1L) } returns jd
        every { experienceReader.signature(1L) } returns "1:2026-01-01T00:00"
        every { recommendationRepository.findByJdId(100L) } returns null
        every { experienceReader.findAllActive(1L) } returns experiences
        every { generateService.generate(jd, experiences) } returns generated
        val savedSlot = slot<JdExperienceRecommendation>()
        every { recommendationRepository.upsert(capture(savedSlot)) } answers { savedSlot.captured.copy(id = 5L) }

        // when
        val result = service.getOrRefresh(1L, "jd-pub-1")

        // then
        result.id shouldBe 5L
        result.jdId shouldBe 100L
        result.items shouldBe generated
        savedSlot.captured.sourceSignature shouldBe "1:2026-01-01T00:00"
        verify(exactly = 1) { generateService.generate(jd, experiences) }
    }

    "캐시된 시그니처가 현재 경험 세트와 다르면 재생성해 갱신한다" {
        // given
        val stale = JdExperienceRecommendation.newInstance(
            jdId = 100L,
            items = listOf(RecommendedExperience(experienceId = 1L, matchRate = 50)),
            sourceSignature = "1:2025-01-01T00:00",
        )
        val experiences = listOf(
            ExperienceFixture.create(id = 1L, workspaceId = 1L),
            ExperienceFixture.create(id = 2L, workspaceId = 1L),
        )
        val generated = listOf(
            RecommendedExperience(experienceId = 2L, matchRate = 95, reason = "새로 추가된 경험"),
            RecommendedExperience(experienceId = 1L, matchRate = 60),
        )
        every { jdRepository.findByPublicIdAndWorkspaceId("jd-pub-1", 1L) } returns jd
        every { experienceReader.signature(1L) } returns "2:2026-01-01T00:00"
        every { recommendationRepository.findByJdId(100L) } returns stale
        every { experienceReader.findAllActive(1L) } returns experiences
        every { generateService.generate(jd, experiences) } returns generated
        every { recommendationRepository.upsert(any()) } answers { firstArg() }

        // when
        val result = service.getOrRefresh(1L, "jd-pub-1")

        // then
        result.items shouldBe generated
        result.sourceSignature shouldBe "2:2026-01-01T00:00"
        verify(exactly = 1) { generateService.generate(jd, experiences) }
        verify(exactly = 1) { recommendationRepository.upsert(any()) }
    }

})

private fun jd(id: Long, publicId: String, workspaceId: Long) = Jd(
    id = id,
    publicId = publicId,
    workspaceId = workspaceId,
    sourceUrl = "https://example.com/jd",
    companyName = "잡도리",
    positionTitle = "백엔드 개발자",
    companyIntro = "채용 도우미 팀",
    responsibilities = emptyList(),
    requiredExperiences = emptyList(),
    preferredExperiences = emptyList(),
    hiringProcess = emptyList(),
    coreCompetencies = emptyList(),
    status = JdStatus.IN_PROGRESS,
)