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

        val result = aiChatClient.generateStructured(
            template.buildStructured(buildUserPrompt(jd, experiences), ExperienceRecommendationResult::class),
        )

        // LLM은 인덱스(1..N)로 참조 → experience로 환원. 점수 정렬을 신뢰하고, 없는 reason은 null.
        val scoreByIndex = result.scores.associate { it.index to it.matchRate }
        val reasonByIndex = result.reasons.associate { it.index to it.reason }
        return experiences.mapIndexed { i, experience ->
            val index = i + 1
            RecommendedExperience(
                experienceId = experience.id,
                matchRate = scoreByIndex[index] ?: 0,
                reason = reasonByIndex[index],
            )
        }.sortedByDescending { it.matchRate }
    }

    private fun buildUserPrompt(jd: Jd, experiences: List<Experience>): String = buildString {
        appendLine("## JD")
        appendLine(JdPromptText.of(jd))
        appendLine()
        appendLine("## 경험 목록")
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
