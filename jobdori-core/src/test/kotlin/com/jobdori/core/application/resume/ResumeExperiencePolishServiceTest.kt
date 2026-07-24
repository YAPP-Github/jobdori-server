package com.jobdori.core.application.resume

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.application.ai.command.AiStructuredRequest
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
    val promptTemplate = PromptTemplate(
        type = PromptType.RESUME_EXPERIENCE_REWRITE,
        modelName = "gpt-4o-mini",
        parameters = AiParameters(temperature = 0.2),
        systemPrompt = "경험을 다듬는다. {tone}",
        jsonSchema = """{"type":"object"}""",
    )
    val jd = Jd(
        id = 1L, publicId = "jd-id", workspaceId = 1L, sourceUrl = null,
        companyName = "잡도리", positionTitle = "백엔드 개발자", companyIntro = "",
        responsibilities = listOf("API 개발"), requiredExperiences = listOf("Kotlin"),
        preferredExperiences = listOf("Spring"), hiringProcess = emptyList(),
        coreCompetencies = listOf("문제 해결"), keyPoints = "안정성", strategy = "성과 강조",
    )

    "여러 contents를 한 번에 첨삭하고 입력 순서대로 응답의 공백을 제거한다" {
        val request = slot<AiStructuredRequest<ResumeExperiencePolishResult>>()
        every { repository.findByType(PromptType.RESUME_EXPERIENCE_REWRITE) } returns promptTemplate
        every { client.generateStructured(capture(request)) } returns ResumeExperiencePolishResult(
            items = listOf(
                ResumeExperiencePolishItem(index = 2, content = "  두 번째 첨삭  "),
                ResumeExperiencePolishItem(index = 1, content = "  첫 번째 첨삭  "),
            ),
        )

        service.polish(listOf("첫 번째 원본", "두 번째 원본"), jd) shouldBe listOf("첫 번째 첨삭", "두 번째 첨삭")
        request.captured.userPrompt.contains("백엔드 개발자") shouldBe true
        request.captured.userPrompt.contains("[1] 첫 번째 원본") shouldBe true
        request.captured.userPrompt.contains("[2] 두 번째 원본") shouldBe true
        request.captured.systemPrompt.contains("{tone}") shouldBe false
    }

    "AI 응답에 중복 index가 있으면 원문을 유지한다" {
        val contents = listOf("첫 번째 원본", "두 번째 원본")
        every { repository.findByType(PromptType.RESUME_EXPERIENCE_REWRITE) } returns promptTemplate
        every { client.generateStructured<ResumeExperiencePolishResult>(any()) } returns ResumeExperiencePolishResult(
            items = listOf(
                ResumeExperiencePolishItem(index = 1, content = "첫 번째 첨삭"),
                ResumeExperiencePolishItem(index = 1, content = "중복 첨삭"),
                ResumeExperiencePolishItem(index = 2, content = "두 번째 첨삭"),
            ),
        )

        service.polish(contents, jd) shouldBe contents
    }
})
