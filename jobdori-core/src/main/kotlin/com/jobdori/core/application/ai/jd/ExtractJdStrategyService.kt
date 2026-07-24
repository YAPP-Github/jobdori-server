package com.jobdori.core.application.ai.jd

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.jd.Jd
import com.jobdori.core.domain.jd.JdPromptText
import com.jobdori.core.domain.profile.ProfileDetail
import com.jobdori.core.domain.profile.ProfileSummaryText
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.stereotype.Service

@Service
class ExtractJdStrategyService(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {
    fun generate(jd: Jd, profile: ProfileDetail): String {
        val template = promptTemplateRepository.findByType(PromptType.JD_APPLICATION_STRATEGY)
            ?: throw AiException("프롬프트 없음: JD_APPLICATION_STRATEGY", AiErrorCode.E500_AI_GENERATION_FAILED)
        return aiChatClient.generateText(template.build(userPrompt = buildUserPrompt(jd, profile)))
    }

    private fun buildUserPrompt(jd: Jd, profile: ProfileDetail): String = buildString {
        appendLine("[JD 정보]")
        appendLine(JdPromptText.of(jd))
        appendLine()
        appendLine("[지원자 정보]")
        append(ProfileSummaryText.of(profile))
    }
}
