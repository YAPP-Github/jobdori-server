package com.jobdori.core.application.jdinsight

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jdinsight.JdInsight
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.stereotype.Service

@Service
class GenerateJdInsightService(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {
    fun generate(jd: Jd): JdInsight {
        val jdText = buildJdText(jd)
        return JdInsight.newInstance(
            jdId = jd.id,
            keyPoints = generateText(PromptType.JD_KEY_POINTS, jdText),
            strategy = generateText(PromptType.JD_APPLICATION_STRATEGY, jdText),
        )
    }

    private fun generateText(type: PromptType, jdText: String): String {
        val template = promptTemplateRepository.findByType(type)
            ?: throw AiException("프롬프트 없음: $type", AiErrorCode.E500_AI_GENERATION_FAILED)
        return aiChatClient.generateText(template.build(jdText))
    }

    // JD 원문은 저장하지 않으므로 구조화 메타를 프롬프트 입력용 텍스트로 재구성한다.
    private fun buildJdText(jd: Jd): String = buildString {
        appendLine("[기업명] ${jd.companyName}")
        appendLine("[포지션] ${jd.positionTitle}")
        appendLine("[기업/팀 소개] ${jd.companyIntro}")
        appendSection("업무 내용", jd.responsibilities)
        appendSection("필요 경험", jd.requiredExperiences)
        appendSection("우대 경험", jd.preferredExperiences)
        appendSection("전형 절차", jd.hiringProcess)
    }.trim()

    private fun StringBuilder.appendSection(label: String, items: List<String>) {
        if (items.isEmpty()) return
        appendLine("[$label]")
        items.forEach { appendLine("- $it") }
    }
}
