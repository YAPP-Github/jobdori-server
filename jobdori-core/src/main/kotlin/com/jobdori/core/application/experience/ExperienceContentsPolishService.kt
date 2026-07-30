package com.jobdori.core.application.experience

import com.jobdori.common.model.Period
import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.experience.StarExperienceContents
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.stereotype.Service

@Service
class ExperienceContentsPolishService(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {

    fun polishFreeStyleToStar(content: String): PolishedExperience {
        val template = promptTemplateRepository.findByType(PromptType.EXPERIENCE_CONTENTS_POLISH)
            ?: throw AiException("경험 내용 다듬기 프롬프트를 찾을 수 없습니다.", AiErrorCode.E500_AI_GENERATION_FAILED)

        val result = aiChatClient.generateStructured(
            template.buildStructured(
                userPrompt = content,
                responseType = ExperienceContentsPolishResult::class,
            ),
        )
        val resolvedResult = if (result.title.isBlank()) {
            aiChatClient.generateStructured(
                template.buildStructured(
                    userPrompt = """
                        $content

                        [필수 보완 지침]
                        title은 위 경험 내용의 핵심 활동을 요약한 간결한 명사형 제목으로 반드시 작성하고,
                        빈 문자열로 반환하지 마라.
                    """.trimIndent(),
                    responseType = ExperienceContentsPolishResult::class,
                ),
            )
        } else {
            result
        }

        return resolvedResult.toDomain(sourceContent = content)
    }

}

internal data class ExperienceContentsPolishResult(
    val title: String = "",
    val period: ExtractedPeriod = ExtractedPeriod(),
    val role: String = "",
    val tags: List<String> = emptyList(),
    val situation: String,
    val task: String,
    val action: String,
    val result: String,
) {
    fun toDomain(sourceContent: String): PolishedExperience {
        val normalizedTags = tags.map(String::trim).filter(String::isNotBlank).distinct().take(MAX_TAG_COUNT)
        val resolvedTitle = title.trim()
            .ifBlank { normalizedTags.take(TITLE_TAG_COUNT).joinToString(" ") }
            .ifBlank { sourceContent.trim().lineSequence().firstOrNull().orEmpty() }
            .take(MAX_TITLE_LENGTH)
            .ifBlank { null }

        return PolishedExperience(
            title = resolvedTitle,
            period = period.toPeriod(),
            role = role.trim().ifBlank { null },
            tags = normalizedTags,
            contents = StarExperienceContents(
                situation = situation,
                task = task,
                action = action,
                result = result,
            ),
        )
    }
}

data class PolishedExperience(
    val title: String?,
    val period: Period?,
    val role: String?,
    val tags: List<String>,
    val contents: StarExperienceContents,
)

private const val MAX_TAG_COUNT = 10
private const val TITLE_TAG_COUNT = 3
private const val MAX_TITLE_LENGTH = 150
