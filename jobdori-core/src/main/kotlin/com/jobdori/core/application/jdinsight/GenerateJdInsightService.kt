package com.jobdori.core.application.jdinsight

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdPromptText
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
        val jdText = JdPromptText.of(jd)
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
}
