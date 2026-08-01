package com.jobdori.core.application.experience

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.core.domain.prompt.PromptTemplate
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDate

class ExperienceContentsPolishServiceTest : StringSpec({

    val promptTemplateRepository = mockk<PromptTemplateRepository>()
    val aiChatClient = mockk<AiChatClient>()
    val service = ExperienceContentsPolishService(
        promptTemplateRepository = promptTemplateRepository,
        aiChatClient = aiChatClient,
    )

    "Free Style 경험 내용을 STAR 구조화 요청으로 변환한다" {
        val requestSlot = slot<AiStructuredRequest<ExperienceContentsPolishResult>>()
        every {
            promptTemplateRepository.findByType(PromptType.EXPERIENCE_CONTENTS_POLISH)
        } returns PromptTemplate(
            type = PromptType.EXPERIENCE_CONTENTS_POLISH,
            modelName = "gpt-4o-mini",
            parameters = AiParameters(temperature = 0.2, maxTokens = 1200),
            systemPrompt = "Free Style 경험 내용을 STAR로 변환한다.",
            jsonSchema = """{"type":"object","required":["title","period","role","tags","situation","task","action","result"]}""",
        )
        every {
            aiChatClient.generateStructured(capture(requestSlot))
        } returns ExperienceContentsPolishResult(
            title = "결제 성능 개선",
            period = ExtractedPeriod(
                startYear = 2025,
                startMonth = 1,
                endYear = 2025,
                endMonth = 3,
            ),
            role = "백엔드 개발자",
            tags = listOf(" Kotlin ", "성능 개선", "", "Kotlin"),
            situation = "상황",
            task = "과제",
            action = "행동",
            result = "결과",
        )

        val response = service.polishFreeStyleToStar("성과를 개선한 경험")

        response.title shouldBe "결제 성능 개선"
        response.period?.startAt shouldBe LocalDate.of(2025, 1, 1)
        response.period?.endAt shouldBe LocalDate.of(2025, 3, 31)
        response.role shouldBe "백엔드 개발자"
        response.tags shouldBe listOf("Kotlin", "성능 개선")
        response.contents.situation shouldBe "상황"
        response.contents.task shouldBe "과제"
        response.contents.action shouldBe "행동"
        response.contents.result shouldBe "결과"
        requestSlot.captured.model shouldBe "gpt-4o-mini"
        requestSlot.captured.systemPrompt shouldBe "Free Style 경험 내용을 STAR로 변환한다."
        requestSlot.captured.userPrompt shouldBe "성과를 개선한 경험"
        requestSlot.captured.parameters shouldBe AiParameters(temperature = 0.2, maxTokens = 1200)
        requestSlot.captured.responseType shouldBe ExperienceContentsPolishResult::class
        requestSlot.captured.jsonSchema shouldBe
            """{"type":"object","required":["title","period","role","tags","situation","task","action","result"]}"""
        verify(exactly = 1) { promptTemplateRepository.findByType(PromptType.EXPERIENCE_CONTENTS_POLISH) }
    }

    "AI가 제목을 비우면 제목 요약을 명시해 다시 생성한다" {
        every {
            promptTemplateRepository.findByType(PromptType.EXPERIENCE_CONTENTS_POLISH)
        } returns PromptTemplate(
            type = PromptType.EXPERIENCE_CONTENTS_POLISH,
            modelName = "gpt-4o-mini",
            parameters = AiParameters(),
            systemPrompt = "Free Style 경험 내용을 STAR로 변환한다.",
            jsonSchema = """{"type":"object"}""",
        )
        every {
            aiChatClient.generateStructured(any<AiStructuredRequest<ExperienceContentsPolishResult>>())
        } returns ExperienceContentsPolishResult(
            title = "",
            tags = listOf("API", "개발"),
            situation = "상황",
            task = "과제",
            action = "행동",
            result = "결과",
        ) andThen ExperienceContentsPolishResult(
            title = "API 개발",
            tags = listOf("API", "개발"),
            situation = "상황",
            task = "과제",
            action = "행동",
            result = "결과",
        )

        val response = service.polishFreeStyleToStar("API 개발 했어요")

        response.title shouldBe "API 개발"
        response.tags shouldBe listOf("API", "개발")
        verify(exactly = 2) {
            aiChatClient.generateStructured(any<AiStructuredRequest<ExperienceContentsPolishResult>>())
        }
    }

})
