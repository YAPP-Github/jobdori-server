package com.jobdori.core.application.resume

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiGenerationRequest
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.prompt.PromptTemplate
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class ResumeExperiencePolishServiceTest : StringSpec({
    val repository = mockk<PromptTemplateRepository>()
    val client = mockk<AiChatClient>()
    val service = ResumeExperiencePolishService(repository, client)

    "JD 정보와 원본 contents로 첨삭하고 응답의 공백을 제거한다" {
        val request = slot<AiGenerationRequest>()
        every { repository.findByType(PromptType.RESUME_EXPERIENCE_REWRITE) } returns PromptTemplate(
            type = PromptType.RESUME_EXPERIENCE_REWRITE,
            modelName = "gpt-4o-mini",
            parameters = AiParameters(temperature = 0.2),
            systemPrompt = "경험을 다듬는다. {tone}",
            jsonSchema = null,
        )
        every { client.generateText(capture(request)) } returns "  첨삭 결과  "
        val jd = Jd(
            id = 1L, publicId = "jd-id", workspaceId = 1L, sourceUrl = null,
            companyName = "잡도리", positionTitle = "백엔드 개발자", companyIntro = "",
            responsibilities = listOf("API 개발"), requiredExperiences = listOf("Kotlin"),
            preferredExperiences = listOf("Spring"), hiringProcess = emptyList(),
            coreCompetencies = listOf("문제 해결"), keyPoints = "안정성", strategy = "성과 강조",
        )

        service.polish("원본 내용", jd) shouldBe "첨삭 결과"
        request.captured.userPrompt.contains("백엔드 개발자") shouldBe true
        request.captured.userPrompt.contains("원본 내용") shouldBe true
        request.captured.systemPrompt.contains("{tone}") shouldBe false
    }
})
