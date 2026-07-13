package com.jobdori.core.application.ai.jd

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.core.application.ai.jd.result.JdPosting
import com.jobdori.core.application.ai.jd.result.JdPostingSplitResult
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.jd.JdPolicy
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

class SplitJdPostingsServiceTest : StringSpec({

    val promptTemplateRepository = mockk<PromptTemplateRepository>()
    val aiChatClient = mockk<AiChatClient>()
    val service = SplitJdPostingsService(
        promptTemplateRepository = promptTemplateRepository,
        aiChatClient = aiChatClient,
    )

    val prompt = PromptTemplate(
        modelName = "gpt-4o-mini",
        parameters = AiParameters(temperature = 0.2, maxTokens = 4096),
        systemPrompt = "본문에서 여러 공고를 분리한다",
        jsonSchema = """{"type":"object"}""",
    )

    "동일 직무명 후보는 중복 제거하고 나머지를 그대로 반환한다" {
        // given
        every { promptTemplateRepository.findByType(PromptType.JD_MULTI_POSTING_SPLIT) } returns prompt
        every {
            aiChatClient.generateStructured(any<AiStructuredRequest<JdPostingSplitResult>>())
        } returns JdPostingSplitResult(
            postings = listOf(
                JdPosting(title = "백엔드 개발자", body = "본문 A"),
                JdPosting(title = "백엔드 개발자", body = "본문 A 중복"),
                JdPosting(title = "프론트 개발자", body = "본문 B"),
            ),
        )

        // when
        val result = service.split("여러 공고가 섞인 본문")

        // then
        result.map { it.title } shouldContainExactly listOf("백엔드 개발자", "프론트 개발자")
        result.map { it.body } shouldContainExactly listOf("본문 A", "본문 B")
    }

    "후보가 최대 개수를 넘으면 상위 MAX_SPLIT_CANDIDATES건만 반환한다" {
        // given
        val postings = (1..10).map { JdPosting(title = "공고 $it", body = "본문 $it") }
        every { promptTemplateRepository.findByType(PromptType.JD_MULTI_POSTING_SPLIT) } returns prompt
        every {
            aiChatClient.generateStructured(any<AiStructuredRequest<JdPostingSplitResult>>())
        } returns JdPostingSplitResult(postings = postings)

        // when
        val result = service.split("여러 공고가 섞인 본문")

        // then
        result.size shouldBe JdPolicy.MAX_SPLIT_CANDIDATES
        result shouldBe postings.take(JdPolicy.MAX_SPLIT_CANDIDATES)
    }

    "AI가 후보를 하나도 추출하지 못하면 원본 본문 그대로 단일 후보를 반환한다" {
        // given
        every { promptTemplateRepository.findByType(PromptType.JD_MULTI_POSTING_SPLIT) } returns prompt
        every {
            aiChatClient.generateStructured(any<AiStructuredRequest<JdPostingSplitResult>>())
        } returns JdPostingSplitResult(postings = emptyList())

        // when
        val result = service.split("단일 공고 본문")

        // then
        result shouldContainExactly listOf(JdPosting(body = "단일 공고 본문"))
    }

    "JD_MULTI_POSTING_SPLIT 프롬프트가 없으면 AiException을 던진다" {
        // given
        every { promptTemplateRepository.findByType(PromptType.JD_MULTI_POSTING_SPLIT) } returns null

        // when & then
        shouldThrow<AiException> {
            service.split("본문")
        }
        verify(exactly = 0) { aiChatClient.generateStructured(any<AiStructuredRequest<JdPostingSplitResult>>()) }
    }

})