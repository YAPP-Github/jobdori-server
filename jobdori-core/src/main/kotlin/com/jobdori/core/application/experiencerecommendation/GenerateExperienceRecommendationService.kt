package com.jobdori.core.application.experiencerecommendation

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.experiencerecommendation.result.ExperienceRecommendationResult
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.experience.Experience
import com.jobdori.core.domain.experience.FreeExperienceContents
import com.jobdori.core.domain.experience.StarExperienceContents
import com.jobdori.core.domain.experiencerecommendation.RecommendedExperience
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdPromptText
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.stereotype.Service

@Service
class GenerateExperienceRecommendationService(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {

    fun generate(jd: Jd, experiences: List<Experience>): List<RecommendedExperience> {
        if (experiences.isEmpty()) return emptyList()

        val template = promptTemplateRepository.findByType(PromptType.EXPERIENCE_RECOMMENDATION)
            ?: throw AiException(
                "프롬프트 없음: ${PromptType.EXPERIENCE_RECOMMENDATION}",
                AiErrorCode.E500_AI_GENERATION_FAILED,
            )

        val structured = template.buildStructured(buildUserPrompt(jd, experiences), ExperienceRecommendationResult::class)

        // 모델이 reasons의 "상위 5개" 규칙을 scores에도 적용해 일부만 채점하는 경우가 있어 1회 재시도한다.
        var lastMissing = emptyList<Int>()
        var lastResponded = 0
        repeat(2) {
            val result = aiChatClient.generateStructured(structured)

            // LLM은 인덱스(1..N)로 참조 -> experience로 환원. 없는 reason은 null.
            val scoreByIndex = result.scores.associate { it.index to it.matchRate }
            val missing = (1..experiences.size).filter { it !in scoreByIndex }
            if (missing.isEmpty()) {
                val reasonByIndex = result.reasons.associate { it.index to it.reason }
                return experiences.mapIndexed { i, experience ->
                    val index = i + 1
                    RecommendedExperience(
                        experienceId = experience.id,
                        matchRate = scoreByIndex.getValue(index),
                        reason = reasonByIndex[index],
                    )
                }.sortedByDescending { it.matchRate }
            }
            lastMissing = missing
            lastResponded = scoreByIndex.size
        }
        throw AiException(
            "경험 점수 누락(재시도 1회 포함): index $lastMissing (전체 ${experiences.size}개 중 ${lastResponded}개 응답)",
            AiErrorCode.E500_AI_GENERATION_FAILED,
        )
    }

    private fun buildUserPrompt(jd: Jd, experiences: List<Experience>): String = buildString {
        appendLine("## JD")
        appendLine(JdPromptText.of(jd))
        appendLine()
        if (jd.strategy.isNotBlank()) {
            appendLine("## 지원 전략")
            appendLine(jd.strategy)
            appendLine()
        }
        appendLine("## 경험 목록 (총 ${experiences.size}개)")
        appendLine("scores에는 아래 ${experiences.size}개 경험을 하나도 빠짐없이 전부 채점해 정확히 ${experiences.size}개 항목을 반환한다. 상위 5개 제한은 reasons에만 적용된다.")
        experiences.forEachIndexed { i, experience ->
            appendLine("[${i + 1}] ${experience.title}")
            if (experience.tags.isNotEmpty()) appendLine("태그: ${experience.tags.joinToString(", ")}")
            appendLine(renderContents(experience))
            appendLine()
        }
    }.trim()

    private fun renderContents(experience: Experience): String = when (val c = experience.contents) {
        is StarExperienceContents -> "상황: ${c.situation}\n과제: ${c.task}\n행동: ${c.action}\n결과: ${c.result}"
        is FreeExperienceContents -> c.content
    }
}
