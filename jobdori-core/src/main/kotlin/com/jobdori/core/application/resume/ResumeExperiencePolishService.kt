package com.jobdori.core.application.resume

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.stereotype.Service

@Service
class ResumeExperiencePolishService(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {

    fun polish(contents: List<String>, jd: Jd): List<String> {
        if (contents.isEmpty()) return emptyList()

        val template = promptTemplateRepository.findByType(PromptType.RESUME_EXPERIENCE_REWRITE)
            ?: throw AiException("이력서 경험 첨삭 프롬프트를 찾을 수 없습니다.", AiErrorCode.E500_AI_GENERATION_FAILED)

        val result = aiChatClient.generateStructured(template.copy(
            systemPrompt = template.systemPrompt.replace(
                "{tone}",
                "원문의 분량과 형식을 최대한 유지하면서 간결하고 전문적인 한국어로 작성한다.",
            ),
        ).buildStructured(buildUserPrompt(contents, jd), ResumeExperiencePolishResult::class))

        val polishedByIndex = result.items.associate { it.index to it.content.trim() }
        // LLM이 유사 경험을 병합/생략하거나 index를 재번호하면 결과가 입력과 어긋난다.
        // 이때 index로 매핑하면 다른 경험에 엉뚱한 내용이 들어가므로, 1:1이 완전할 때만 첨삭을 적용하고 아니면 원문을 유지한다.
        val hasExactMatch = result.items.size == contents.size &&
            polishedByIndex.keys == (1..contents.size).toSet() &&
            polishedByIndex.values.none { it.isBlank() }
        if (!hasExactMatch) return contents
        return contents.indices.map { index -> polishedByIndex.getValue(index + 1) }
    }

    private fun buildUserPrompt(contents: List<String>, jd: Jd): String = """
        ## 대상 JD
        회사: ${jd.companyName}
        포지션: ${jd.positionTitle}
        주요 업무: ${jd.responsibilities.joinToString("; ")}
        필요 경험: ${jd.requiredExperiences.joinToString("; ")}
        우대 경험: ${jd.preferredExperiences.joinToString("; ")}
        핵심 역량: ${jd.coreCompetencies.joinToString("; ")}
        공고 핵심: ${jd.keyPoints}
        지원 전략: ${jd.strategy}

        ## 첨삭할 경험 contents 목록
        ${contents.mapIndexed { index, content -> "[${index + 1}] $content" }.joinToString("\n")}
    """.trimIndent()
}

internal data class ResumeExperiencePolishResult(
    val items: List<ResumeExperiencePolishItem>,
)

// title은 프롬프트 스키마가 반환을 강제하지만 저장에는 쓰지 않는다 (경험명은 사용자 입력 유지, 테스트 페이지 확인용)
internal data class ResumeExperiencePolishItem(
    val index: Int,
    val title: String = "",
    val content: String,
)
