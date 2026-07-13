package com.jobdori.core.application.ai.jd

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.core.application.ai.jd.result.JdMetaResult
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.prompt.PromptTemplate
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class ExtractJdMetaServiceTest : StringSpec({

    val promptTemplateRepository = mockk<PromptTemplateRepository>()
    val aiChatClient = mockk<AiChatClient>()
    val service = ExtractJdMetaService(
        promptTemplateRepository = promptTemplateRepository,
        aiChatClient = aiChatClient,
    )

    val prompt = PromptTemplate(
        modelName = "gpt-4o-mini",
        parameters = AiParameters(temperature = 0.2, maxTokens = 4096),
        systemPrompt = "JD에서 메타 정보를 추출한다",
        jsonSchema = """{"type":"object"}""",
    )

    "JD 본문에서 AI 구조화 응답을 받아 JdMetaResult를 반환한다" {
        // given
        val metaResult = JdMetaResult(
            companyName = "잡도리",
            positionTitle = "백엔드 개발자",
            companyIntro = "채용 도우미 팀",
            responsibilities = listOf("API 설계"),
            requiredExperiences = listOf("Kotlin 3년 이상"),
            preferredExperiences = listOf("Spring Boot 경험"),
            hiringProcess = listOf("서류", "면접"),
            coreCompetencies = listOf("협업", "문제 해결"),
        )
        every { promptTemplateRepository.findByType(PromptType.JD_META_EXTRACTION) } returns prompt
        every {
            aiChatClient.generateStructured(any<AiStructuredRequest<JdMetaResult>>())
        } returns metaResult

        // when
        val result = service.extractFromBody("JD 본문")

        // then
        result shouldBe metaResult
        result.responsibilities.shouldContainExactly("API 설계")
        verify(exactly = 1) { promptTemplateRepository.findByType(PromptType.JD_META_EXTRACTION) }
        verify(exactly = 1) { aiChatClient.generateStructured(any<AiStructuredRequest<JdMetaResult>>()) }
    }

    "JD_META_EXTRACTION 프롬프트가 없으면 AiException을 던진다" {
        // given
        every { promptTemplateRepository.findByType(PromptType.JD_META_EXTRACTION) } returns null

        // when & then
        shouldThrow<AiException> {
            service.extractFromBody("JD 본문")
        }
        verify(exactly = 0) { aiChatClient.generateStructured(any<AiStructuredRequest<JdMetaResult>>()) }
    }

})