package com.jobdori.core.application.experiencerecommendation

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.core.application.experiencerecommendation.result.ExperienceRecommendationResult
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.ExperienceContents
import com.jobdori.core.domain.experience.ExperienceStatus
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdStatus
import com.jobdori.core.domain.prompt.PromptTemplate
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal

class GenerateExperienceRecommendationServiceTest : StringSpec({

    val promptTemplateRepository = mockk<PromptTemplateRepository>()
    val aiChatClient = mockk<AiChatClient>()
    val service = GenerateExperienceRecommendationService(
        promptTemplateRepository = promptTemplateRepository,
        aiChatClient = aiChatClient,
    )

    val prompt = PromptTemplate(
        modelName = "gpt-4o-mini",
        parameters = AiParameters(temperature = 0.2, maxTokens = 4096),
        systemPrompt = "JD와 경험의 매칭률을 계산한다",
        jsonSchema = """{"type":"object"}""",
    )

    val jd = Jd(
        id = 100L,
        publicId = "jd-pub-1",
        workspaceId = 1L,
        sourceUrl = null,
        sourceBody = "본문",
        companyName = "잡도리",
        positionTitle = "백엔드 개발자",
        companyIntro = "채용 도우미 팀",
        responsibilities = listOf("API 설계"),
        requiredExperiences = listOf("Kotlin 3년 이상"),
        preferredExperiences = listOf("Spring Boot 경험"),
        hiringProcess = listOf("서류", "면접"),
        coreCompetencies = listOf("협업"),
        status = JdStatus.IN_PROGRESS,
    )

    "경험 목록이 비어 있으면 AI 호출 없이 빈 목록을 반환한다" {
        // when
        val result = service.generate(jd, emptyList())

        // then
        result shouldBe emptyList()
        verify(exactly = 0) { promptTemplateRepository.findByType(any()) }
        verify(exactly = 0) { aiChatClient.generateStructured(any<AiStructuredRequest<ExperienceRecommendationResult>>()) }
    }

    "인덱스 기준으로 점수·이유를 경험에 매핑하고 매칭률 내림차순으로 정렬한다" {
        // given
        val experiences = listOf(
            starExperience(id = 1L, title = "지원 현황 API 설계"),
            starExperience(id = 2L, title = "사용자 인증 개선"),
            freeExperience(id = 3L, title = "사이드 프로젝트"),
        )
        every { promptTemplateRepository.findByType(PromptType.EXPERIENCE_RECOMMENDATION) } returns prompt
        every {
            aiChatClient.generateStructured(any<AiStructuredRequest<ExperienceRecommendationResult>>())
        } returns ExperienceRecommendationResult(
            scores = listOf(
                ExperienceRecommendationResult.Score(index = 1, matchRate = 40),
                ExperienceRecommendationResult.Score(index = 2, matchRate = 90),
                // index 3은 점수가 누락됨 -> 0으로 기본값 처리
            ),
            reasons = listOf(
                ExperienceRecommendationResult.Reason(index = 2, reason = "핵심 역량과 맞닿아 있어요."),
            ),
        )

        // when
        val result = service.generate(jd, experiences)

        // then
        result.map { it.experienceId } shouldContainExactly listOf(2L, 1L, 3L)
        result.first().matchRate shouldBe 90
        result.first().reason shouldBe "핵심 역량과 맞닿아 있어요."
        result[1].matchRate shouldBe 40
        result[1].reason shouldBe null
        result[2].matchRate shouldBe 0
        result[2].reason shouldBe null
    }

    "AI 호출 시 JD와 경험 정보를 프롬프트에 담아 전달한다" {
        // given
        val experiences = listOf(starExperience(id = 1L, title = "지원 현황 API 설계"))
        every { promptTemplateRepository.findByType(PromptType.EXPERIENCE_RECOMMENDATION) } returns prompt
        val requestSlot = slot<AiStructuredRequest<ExperienceRecommendationResult>>()
        every {
            aiChatClient.generateStructured(capture(requestSlot))
        } returns ExperienceRecommendationResult(
            scores = listOf(ExperienceRecommendationResult.Score(index = 1, matchRate = 70)),
        )

        // when
        service.generate(jd, experiences)

        // then
        val userPrompt = requestSlot.captured.userPrompt
        userPrompt shouldContain "[기업명] 잡도리"
        userPrompt shouldContain "[포지션] 백엔드 개발자"
        userPrompt shouldContain "- API 설계"
        userPrompt shouldContain "- Kotlin 3년 이상"
        userPrompt shouldContain "[1] 지원 현황 API 설계"
        userPrompt shouldContain "태그: Kotlin, Spring"
        userPrompt shouldContain "상황: 상황"
        userPrompt shouldContain "결과: 결과"
    }

    "EXPERIENCE_RECOMMENDATION 프롬프트가 없으면 AiException을 던진다" {
        // given
        every { promptTemplateRepository.findByType(PromptType.EXPERIENCE_RECOMMENDATION) } returns null

        // when & then
        shouldThrow<AiException> {
            service.generate(jd, listOf(starExperience(id = 1L, title = "경험")))
        }
        verify(exactly = 0) { aiChatClient.generateStructured(any<AiStructuredRequest<ExperienceRecommendationResult>>()) }
    }

})

private fun starExperience(id: Long, title: String) = Experience(
    id = id,
    workspaceId = 1L,
    projectId = 1L,
    tags = listOf("Kotlin", "Spring"),
    title = title,
    contents = ExperienceContents.star("상황", "과제", "행동", "결과"),
    displayOrder = BigDecimal.ZERO,
    status = ExperienceStatus.ACTIVE,
)

private fun freeExperience(id: Long, title: String) = Experience(
    id = id,
    workspaceId = 1L,
    projectId = 1L,
    tags = emptyList(),
    title = title,
    contents = ExperienceContents.free("자유 서술 내용"),
    displayOrder = BigDecimal.ZERO,
    status = ExperienceStatus.ACTIVE,
)