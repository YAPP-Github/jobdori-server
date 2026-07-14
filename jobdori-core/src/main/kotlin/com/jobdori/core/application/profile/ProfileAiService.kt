package com.jobdori.core.application.profile

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.prompt.PromptTemplate
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import com.jobdori.core.domain.profile.ProfileDetail
import org.springframework.stereotype.Service

@Service
class ProfileAiService(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {

    fun generateCoreCompetency(detail: ProfileDetail): String {
        val template = getTemplate(PromptType.PROFILE_CORE_COMPETENCY_GENERATION)

        return aiChatClient.generateText(template.build(userPrompt = buildProfileSummaryPrompt(detail)))
    }

    fun polish(text: String, kind: ProfilePolishKind): String {
        val template = getTemplate(PromptType.PROFILE_TEXT_POLISH)
        val userPrompt = "[항목] ${kind.label}\n[글자수 제한] ${kind.maxLength}자\n[원문]\n$text"

        return aiChatClient.generateText(template.build(userPrompt = userPrompt))
    }

    private fun getTemplate(type: PromptType): PromptTemplate {
        return promptTemplateRepository.findByType(type)
            ?: throw AiException("이력서 AI 프롬프트를 찾을 수 없습니다. [type=$type]", AiErrorCode.E500_AI_GENERATION_FAILED)
    }

    private fun buildProfileSummaryPrompt(detail: ProfileDetail): String = buildString {
        appendLine("[경력]")
        detail.sections.careers.forEach {
            appendLine(
                "- ${it.company.orEmpty()} / ${it.position.orEmpty()} " +
                    "(${it.period?.startAt ?: ""} - ${it.period?.endAt ?: ""}): ${it.description.orEmpty()}",
            )
        }
        appendLine("[스킬]")
        append(detail.sections.skills.joinToString(", ") { it.name.orEmpty() })
    }

}

enum class ProfilePolishKind(val label: String, val maxLength: Int) {
    CORE_COMPETENCY("핵심역량", 500),
    CAREER_DESCRIPTION("경력 세부 내용", 500),
    EXPERIENCE_TITLE("경험명", 48),
    EXPERIENCE_DESCRIPTION("경험 STAR 설명", 600),
}
