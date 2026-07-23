package com.jobdori.core.application.profile

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.error.JdNotFoundException
import com.jobdori.core.domain.jd.repository.JdRepository
import com.jobdori.core.domain.prompt.PromptTemplate
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import com.jobdori.core.domain.profile.ProfileDetail
import org.springframework.stereotype.Service

// 생성 결과 + FE 표시용 JD 지원 전략(jd.strategy)을 함께 노출한다. 전략은 프롬프트 입력에는 쓰지 않는다.
data class CoreCompetencyGeneration(
    val strategy: String?,
    val coreCompetency: String,
)

@Service
class ProfileAiService(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
    private val jdRepository: JdRepository,
) {

    fun generateCoreCompetency(detail: ProfileDetail, workspaceId: Long, jdPublicId: String?): CoreCompetencyGeneration {
        val jd = findJd(workspaceId, jdPublicId)
        val strategy = jd?.strategy?.takeIf { it.isNotBlank() }

        val template = getTemplate(PromptType.PROFILE_CORE_COMPETENCY_GENERATION)
        val text = aiChatClient.generateText(
            template.build(userPrompt = buildProfileSummaryPrompt(detail, jd?.sourceBody)),
        )
        return CoreCompetencyGeneration(strategy, text)
    }

    fun polish(
        text: String,
        kind: ProfilePolishKind,
        structure: PolishStructure? = null,
        instruction: String? = null,
        title: String? = null,
        workspaceId: Long? = null,
        jdPublicId: String? = null,
    ): String {
        val strategy = workspaceId?.let { resolveStrategy(it, jdPublicId) }
        val template = getTemplate(PromptType.PROFILE_TEXT_POLISH)
        val userPrompt = buildString {
            appendLine("[항목] ${kind.label}")
            appendLine("[글자수 제한] ${kind.maxLength}자")
            structure?.let { appendLine("[작성 구조] ${it.instruction}") }
            instruction?.takeIf { it.isNotBlank() }?.let { appendLine("[추가 지침] $it") }
            title?.takeIf { it.isNotBlank() }?.let { appendLine("[경험명] $it") }
            strategy?.let {
                appendLine("[지원 전략]")
                appendLine(it)
            }
            appendLine("[원문]")
            append(text)
        }

        return aiChatClient.generateText(template.build(userPrompt = userPrompt))
    }

    private fun resolveStrategy(workspaceId: Long, jdPublicId: String?): String? {
        return findJd(workspaceId, jdPublicId)?.strategy?.takeIf { it.isNotBlank() }
    }

    private fun findJd(workspaceId: Long, jdPublicId: String?): Jd? {
        return jdPublicId?.let {
            jdRepository.findByPublicIdAndWorkspaceId(it, workspaceId)
                ?: throw JdNotFoundException("등록되지 않은 JD($it)입니다")
        }
    }

    private fun getTemplate(type: PromptType): PromptTemplate {
        return promptTemplateRepository.findByType(type)
            ?: throw AiException("이력서 AI 프롬프트를 찾을 수 없습니다. [type=$type]", AiErrorCode.E500_AI_GENERATION_FAILED)
    }

    private fun buildProfileSummaryPrompt(detail: ProfileDetail, jdBody: String?): String = buildString {
        // sourceBody는 기능 이전 레거시 JD 행에서 null → 그 경우 [JD] 섹션 생략
        if (!jdBody.isNullOrBlank()) {
            appendLine("[JD]")
            appendLine(jdBody)
        }
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
    EXPERIENCE_DESCRIPTION("경험 STAR 설명", 500),
}

enum class PolishStructure(val instruction: String) {
    BULLET("각 항목이 '- '로 시작하는 불렛 목록으로 작성한다."),
    PROBLEM_SOLUTION_RESULT("'-(문제) ', '-(해결) ', '-(성과) '로 시작하는 세 줄로 작성한다."),
    PROSE("자연스러운 산문형 문단으로 작성한다."),
}
