package com.jobdori.core.application.experience

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.experience.StarExperienceContents
import com.jobdori.core.domain.prompt.PromptTemplate
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate

class ExperienceAiExtractionServiceTest : StringSpec({

    val aiChatClient = mockk<AiChatClient>()
    val promptTemplateRepository = mockk<PromptTemplateRepository>()
    val service = ExperienceAiExtractionService(
        aiChatClient = aiChatClient,
        promptTemplateRepository = promptTemplateRepository,
    )

    beforeTest {
        clearMocks(aiChatClient, promptTemplateRepository)
    }

    "AI 구조화 응답을 가져온 경험 저장 커맨드로 변환한다" {
        val prompt = PromptTemplate(
            type = PromptType.EXPERIENCE_STAR_EXTRACTION,
            modelName = "gpt-4o-mini",
            parameters = AiParameters(temperature = 0.2, maxTokens = 4096),
            systemPrompt = "경험을 STAR로 추출한다",
            jsonSchema = """{"type":"object"}""",
        )
        val extractionResult = ExperienceStarExtractionResult(
            projects = listOf(
                ExtractedExperienceProject(
                    name = "채용 플랫폼",
                    summary = "지원 경험 관리 프로젝트",
                    period = ExtractedPeriod(
                        startYear = 2025,
                        startMonth = 1,
                        endYear = 2025,
                        endMonth = 4,
                        isCurrent = false,
                    ),
                    role = "백엔드 개발",
                    company = "잡도리",
                    experiences = listOf(
                        ExtractedExperience(
                            title = "지원 현황 API 설계",
                            situation = "지원 상태가 흩어져 있었다",
                            task = "상태 관리 API가 필요했다",
                            action = "커서 기반 API를 설계했다",
                            result = "조회 흐름이 단순해졌다",
                            competencyTags = listOf("Kotlin", "Spring", "Kotlin", " "),
                        ),
                    ),
                ),
                ExtractedExperienceProject(
                    name = "",
                    experiences = listOf(
                        ExtractedExperience(title = "저장되지 않을 경험", action = "프로젝트명이 없다"),
                    ),
                ),
            ),
        )

        every { promptTemplateRepository.findByType(PromptType.EXPERIENCE_STAR_EXTRACTION) } returns prompt
        every {
            aiChatClient.generateStructured(any<AiStructuredRequest<ExperienceStarExtractionResult>>())
        } returns extractionResult

        val groups = service.extract("PDF 원문").toCommandGroups()

        groups.size shouldBe 1
        val group = groups.single()
        group.project.name shouldBe "채용 플랫폼"
        group.project.summary shouldBe "지원 경험 관리 프로젝트"
        group.project.period?.startAt shouldBe LocalDate.of(2025, 1, 1)
        group.project.period?.endAt shouldBe LocalDate.of(2025, 4, 30)
        group.project.role shouldBe "백엔드 개발"

        val experience = group.experiences.single()
        experience.title shouldBe "지원 현황 API 설계"
        experience.tags.shouldContainExactly("Kotlin", "Spring")
        experience.contents shouldBe StarExperienceContents(
            situation = "지원 상태가 흩어져 있었다",
            task = "상태 관리 API가 필요했다",
            action = "커서 기반 API를 설계했다",
            result = "조회 흐름이 단순해졌다",
        )

        verify(exactly = 1) { promptTemplateRepository.findByType(PromptType.EXPERIENCE_STAR_EXTRACTION) }
        verify(exactly = 1) {
            aiChatClient.generateStructured(any<AiStructuredRequest<ExperienceStarExtractionResult>>())
        }
    }

    "AI가 구조화한 현재 진행 프로젝트 기간을 변환한다" {
        val project = ExtractedExperienceProject(
            name = "푸시 플랫폼 개발 및 운영",
            summary = "푸시 플랫폼 개발 및 운영",
            period = ExtractedPeriod(
                startYear = 22,
                startMonth = 1,
                endYear = null,
                endMonth = null,
                isCurrent = true,
            ),
            role = "백엔드 개발",
            experiences = listOf(
                ExtractedExperience(
                    title = "푸시 플랫폼 운영",
                    action = "푸시 플랫폼을 개발하고 운영했다",
                ),
            ),
        )

        val group = project.toCommandGroup()

        group?.project?.period?.startAt shouldBe LocalDate.of(2022, 1, 1)
        group?.project?.period?.endAt shouldBe null
    }

    "구조화 기간이 없으면 기존 문자열 기간을 fallback으로 변환한다" {
        val project = ExtractedExperienceProject(
            name = "시스템 개발 및 운영",
            summary = "시스템 개발 및 운영",
            periodText = "22.01 ~ 현재",
            role = "백엔드 개발",
            experiences = listOf(
                ExtractedExperience(
                    title = "시스템 운영",
                    action = "시스템을 개발하고 운영했다",
                ),
            ),
        )

        val group = project.toCommandGroup()

        group?.project?.period?.startAt shouldBe LocalDate.of(2022, 1, 1)
        group?.project?.period?.endAt shouldBe null
    }

    "경험 기간과 역할을 우선 사용하고 비어 있으면 프로젝트 값을 사용한다" {
        val project = ExtractedExperienceProject(
            name = "채용 플랫폼",
            period = ExtractedPeriod(startYear = 2024, startMonth = 1, endYear = 2024, endMonth = 12),
            role = "프로젝트 역할",
            experiences = listOf(
                ExtractedExperience(
                    title = "개별 역할 경험",
                    period = ExtractedPeriod(startYear = 2024, startMonth = 3, endYear = 2024, endMonth = 6),
                    role = "경험 역할",
                    action = "기능을 개발했다",
                ),
                ExtractedExperience(
                    title = "프로젝트 값 상속 경험",
                    action = "서비스를 운영했다",
                ),
            ),
        )

        val experiences = project.toCommandGroup()?.experiences.orEmpty()

        experiences[0].period?.startAt shouldBe LocalDate.of(2024, 3, 1)
        experiences[0].period?.endAt shouldBe LocalDate.of(2024, 6, 30)
        experiences[0].role shouldBe "경험 역할"
        experiences[1].period?.startAt shouldBe LocalDate.of(2024, 1, 1)
        experiences[1].period?.endAt shouldBe LocalDate.of(2024, 12, 31)
        experiences[1].role shouldBe "프로젝트 역할"
    }

    "경험 추출 프롬프트가 없으면 AI 예외를 던진다" {
        every { promptTemplateRepository.findByType(PromptType.EXPERIENCE_STAR_EXTRACTION) } returns null

        shouldThrow<AiException> {
            service.extract("PDF 원문")
        }

        verify(exactly = 0) {
            aiChatClient.generateStructured(any<AiStructuredRequest<ExperienceStarExtractionResult>>())
        }
    }

})
