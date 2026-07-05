package com.jobdori.core.application.experience

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.core.domain.experience.StarExperienceContents
import com.jobdori.core.domain.prompt.PromptTemplate
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify

class ExperienceContentsPolishServiceTest : StringSpec({

    val promptTemplateRepository = mockk<PromptTemplateRepository>()
    val aiChatClient = mockk<AiChatClient>()
    val service = ExperienceContentsPolishService(
        promptTemplateRepository = promptTemplateRepository,
        aiChatClient = aiChatClient,
    )

    "Free Style 경험 내용을 STAR 구조화 요청으로 변환한다" {
        val requestSlot = slot<AiStructuredRequest<StarExperienceContents>>()
        every {
            promptTemplateRepository.findByType(PromptType.EXPERIENCE_CONTENTS_POLISH)
        } returns PromptTemplate(
            modelName = "gpt-4o-mini",
            parameters = AiParameters(temperature = 0.2, maxTokens = 1200),
            systemPrompt = "Free Style 경험 내용을 STAR로 변환한다.",
            jsonSchema = """{"type":"object","required":["situation","task","action","result"]}""",
        )
        every {
            aiChatClient.generateStructured(capture(requestSlot))
        } returns StarExperienceContents(
            situation = "상황",
            task = "과제",
            action = "행동",
            result = "결과",
        )

        val response = service.polishFreeStyleToStar("성과를 개선한 경험")

        response.situation shouldBe "상황"
        response.task shouldBe "과제"
        response.action shouldBe "행동"
        response.result shouldBe "결과"
        requestSlot.captured.model shouldBe "gpt-4o-mini"
        requestSlot.captured.systemPrompt shouldBe "Free Style 경험 내용을 STAR로 변환한다."
        requestSlot.captured.userPrompt shouldBe "성과를 개선한 경험"
        requestSlot.captured.parameters shouldBe AiParameters(temperature = 0.2, maxTokens = 1200)
        requestSlot.captured.responseType shouldBe StarExperienceContents::class
        requestSlot.captured.jsonSchema shouldBe """{"type":"object","required":["situation","task","action","result"]}"""
        verify(exactly = 1) { promptTemplateRepository.findByType(PromptType.EXPERIENCE_CONTENTS_POLISH) }
    }

})
