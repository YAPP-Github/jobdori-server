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
import com.jobdori.core.domain.profile.ProfilePolicy
import com.jobdori.core.domain.profile.ProfileSummaryText
import com.jobdori.core.domain.resume.ResumeDetail
import com.jobdori.core.domain.resume.ResumeExperiencePayload
import com.jobdori.core.domain.resume.ResumeSectionType
import org.springframework.stereotype.Service

// 생성 결과 + FE 표시용 JD 지원 전략(jd.strategy)을 함께 노출한다. 전략은 프롬프트 입력에는 쓰지 않는다.
data class CoreCompetencyGeneration(
    val strategy: String?,
    val coreCompetency: String,
)

/** 필드가 data.sql PROFILE_TEXT_POLISH jsonSchema와 1:1 대응한다. */
data class ExperiencePolish(
    val title: String,
    val description: String,
)

@Service
class ProfileAiService(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
    private val jdRepository: JdRepository,
) {

    fun generateCoreCompetency(
        detail: ProfileDetail,
        resumeDetail: ResumeDetail,
        workspaceId: Long,
        jdPublicId: String?,
    ): CoreCompetencyGeneration {
        val jd = findJd(workspaceId, jdPublicId)
        val strategy = jd?.strategy?.takeIf { it.isNotBlank() }

        val template = getTemplate(PromptType.PROFILE_CORE_COMPETENCY_GENERATION)
        val text = aiChatClient.generateText(
            template.build(userPrompt = buildProfileSummaryPrompt(detail, resumeDetail, jd?.sourceBody)),
        )
        return CoreCompetencyGeneration(strategy, text)
    }

    fun polish(
        text: String,
        kind: ProfilePolishKind,
        structure: PolishStructure? = null,
        instruction: String? = null,
        workspaceId: Long? = null,
        jdPublicId: String? = null,
    ): String {
        val strategy = workspaceId?.let { resolveStrategy(it, jdPublicId) }
        val template = getTemplate(PromptType.PROFILE_TEXT_POLISH)
        val userPrompt = buildString {
            appendLine("[항목] ${kind.label}")
            appendLine("[결과 글자수 제한] ${kind.maxLength}자")
            structure?.let { appendLine("[작성 구조] ${it.instruction}") }
            instruction?.takeIf { it.isNotBlank() }?.let { appendLine("[추가 지침] $it") }
            strategy?.let {
                appendLine("[지원 전략]")
                appendLine(it)
            }
            appendLine("[원문]")
            append(text)
        }

        val result = aiChatClient.generateText(template.build(userPrompt = userPrompt)).trim()
        // LLM 응답이 프로필 저장 한도를 넘지 않도록 첨삭 결과 제한에 맞춘다.
        return result.takeIf { it.isNotBlank() }
            ?.take(kind.maxLength)
            ?: text
    }

    fun polishExperience(
        title: String,
        description: String,
        structure: PolishStructure? = null,
        instruction: String? = null,
        workspaceId: Long? = null,
        jdPublicId: String? = null,
    ): ExperiencePolish {
        val strategy = workspaceId?.let { resolveStrategy(it, jdPublicId) }
        val template = getTemplate(PromptType.PROFILE_TEXT_POLISH)
        val userPrompt = buildString {
            appendLine("[경험명] $title")
            appendLine("[경험명 글자수 제한] ${ProfilePolicy.MAX_TITLE_LENGTH}자")
            appendLine("[경험 내용 결과 글자수 제한] ${EXPERIENCE_DESCRIPTION_RESULT_MAX_LENGTH}자")
            structure?.let { appendLine("[경험 내용 작성 구조] ${it.instruction}") }
            instruction?.takeIf { it.isNotBlank() }?.let { appendLine("[추가 지침] $it") }
            strategy?.let {
                appendLine("[지원 전략]")
                appendLine(it)
            }
            appendLine("[경험 내용]")
            append(description)
        }
        val result = aiChatClient.generateStructured(
            template.buildStructured(userPrompt, ExperiencePolish::class),
        )

        // 경험명 수정 지침이 없을 때 원문 유지 계약을 LLM 응답과 무관하게 보장한다.
        val polishedTitle = if (instruction.isNullOrBlank()) {
            title
        } else {
            result.title.trim().ifBlank { title }.take(ProfilePolicy.MAX_TITLE_LENGTH)
        }
        // LLM 응답은 긴 원문과 별개로 간결한 이력서용 결과 제한을 지켜야 한다.
        return ExperiencePolish(
            title = polishedTitle,
            description = result.description.trim().takeIf { it.isNotBlank() }
                ?.take(EXPERIENCE_DESCRIPTION_RESULT_MAX_LENGTH)
                ?: description,
        )
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

    private fun buildProfileSummaryPrompt(
        detail: ProfileDetail,
        resumeDetail: ResumeDetail,
        jdBody: String?,
    ): String {
        val blocks = mutableListOf<String>()

        // sourceBody는 기능 이전 레거시 JD 행에서 null → 그 경우 [JD] 섹션 생략
        jdBody?.takeIf { it.isNotBlank() }?.let { blocks += "[JD]\n$it" }
        ProfileSummaryText.of(detail).takeIf { it.isNotBlank() }?.let { blocks += it }
        buildSelectedExperienceText(resumeDetail)?.let { blocks += it }

        return blocks.joinToString("\n\n")
    }

    private fun buildSelectedExperienceText(resumeDetail: ResumeDetail): String? {
        val experiences = resumeDetail.sections
            .asSequence()
            .filter { it.section.type == ResumeSectionType.EXPERIENCE && it.section.visible }
            .sortedBy { it.section.displayOrder }
            .flatMap { section ->
                section.items
                    .asSequence()
                    .filter { it.visible }
                    .sortedBy { it.displayOrder }
            }
            .mapNotNull { it.payload as? ResumeExperiencePayload }
            .filter(::hasContent)
            .toList()

        if (experiences.isEmpty()) return null
        return buildString {
            appendLine("[이력서에서 선택한 경험]")
            experiences.forEachIndexed { index, experience ->
                if (index > 0) appendLine()
                appendLine("[${index + 1}]")
                experience.name.nonBlank()?.let { appendLine("경험명: $it") }
                experience.role.nonBlank()?.let { appendLine("역할: $it") }
                experience.period?.let { period ->
                    if (period.startAt != null || period.endAt != null) {
                        appendLine("기간: ${period.startAt ?: ""} - ${period.endAt ?: ""}")
                    }
                }
                experience.contents.nonBlank()?.let { appendLine("내용: $it") }
            }
        }.trim()
    }

    private fun hasContent(experience: ResumeExperiencePayload): Boolean =
        experience.name.nonBlank() != null ||
            experience.role.nonBlank() != null ||
            experience.contents.nonBlank() != null ||
            experience.period?.let { it.startAt != null || it.endAt != null } == true

    private fun String?.nonBlank(): String? = this?.takeIf { it.isNotBlank() }

}

// 저장 한도와 별개로 간결한 AI 첨삭 결과를 만드는 출력 정책이다.
const val EXPERIENCE_DESCRIPTION_RESULT_MAX_LENGTH = 500

enum class ProfilePolishKind(val label: String, val maxLength: Int) {
    CORE_COMPETENCY("핵심역량", ProfilePolicy.MAX_CORE_COMPETENCY_LENGTH),
    CAREER_DESCRIPTION("경력 세부 내용", ProfilePolicy.MAX_CAREER_DESCRIPTION_LENGTH),
    EXPERIENCE("경험 내용", EXPERIENCE_DESCRIPTION_RESULT_MAX_LENGTH),
}

enum class PolishStructure(val instruction: String) {
    BULLET("각 항목이 '- '로 시작하는 불렛 목록으로 작성한다."),
    PROBLEM_SOLUTION_RESULT("'-(문제) ', '-(해결) ', '-(성과) '로 시작하는 세 줄로 작성한다."),
    PROSE("자연스러운 산문형 문단으로 작성한다."),
}
