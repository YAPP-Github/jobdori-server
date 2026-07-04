package com.jobdori.core.application.ai.jd

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.jd.result.JdMetaResult
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.stereotype.Service

@Service
class ExtractJdMetaService(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {
    fun extractFromBody(body: String): JdMetaResult {
        val template = promptTemplateRepository.findByType(PromptType.JD_META_EXTRACTION)
            ?: throw AiException("프롬프트 없음: JD_META_EXTRACTION", AiErrorCode.E500_AI_GENERATION_FAILED)
        return aiChatClient.generateStructured(template.buildStructured(body, JdMetaResult::class))
    }
}
