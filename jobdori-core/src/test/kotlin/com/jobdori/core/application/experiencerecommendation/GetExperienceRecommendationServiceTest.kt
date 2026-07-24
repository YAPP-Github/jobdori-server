package com.jobdori.core.application.experiencerecommendation

import com.jobdori.core.application.ai.jd.ExtractJdStrategyService
import com.jobdori.core.domain.experience.service.ExperienceReader
import com.jobdori.core.domain.experiencerecommendation.JdExperienceRecommendation
import com.jobdori.core.domain.experiencerecommendation.repository.JdExperienceRecommendationRepository
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.repository.JdRepository
import com.jobdori.core.domain.profile.service.ProfileReader
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class GetExperienceRecommendationServiceTest : StringSpec({

    val jdRepository = mockk<JdRepository>()
    val experienceReader = mockk<ExperienceReader>()
    val recommendationRepository = mockk<JdExperienceRecommendationRepository>()
    val generateService = mockk<GenerateExperienceRecommendationService>()
    val extractJdStrategyService = mockk<ExtractJdStrategyService>()
    val profileReader = mockk<ProfileReader>()
    val service = GetExperienceRecommendationService(
        jdRepository = jdRepository,
        experienceReader = experienceReader,
        recommendationRepository = recommendationRepository,
        generateService = generateService,
        extractJdStrategyService = extractJdStrategyService,
        profileReader = profileReader,
    )

    "프로필 조회에 실패하면 기존 JD로 경험 추천을 계속한다" {
        val jd = mockk<Jd> {
            every { id } returns 1L
            every { workspaceId } returns 10L
            every { strategy } returns ""
        }
        every { jdRepository.findByPublicIdAndWorkspaceId("jd-id", 10L) } returns jd
        every { profileReader.getOrCreateProfile(10L) } throws IllegalStateException("프로필 조회 실패")
        every { experienceReader.signature(10L) } returns "signature"
        every { recommendationRepository.findByJdId(1L) } returns JdExperienceRecommendation(
            id = 1L,
            jdId = 1L,
            items = emptyList(),
            sourceSignature = "signature",
        )

        service.getOrRefresh(workspaceId = 10L, jdPublicId = "jd-id") shouldBe
            ExperienceRecommendationView(strategy = "", items = emptyList())

        verify(exactly = 0) { extractJdStrategyService.generate(any(), any()) }
        verify(exactly = 0) { jdRepository.save(any()) }
    }

})
