package com.jobdori.core.application.experience

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

    fun polishFreeStyleToStar(content: String): StarExperienceContents {
        val template = promptTemplateRepository.findByType(PromptType.EXPERIENCE_CONTENTS_POLISH)
            ?: throw AiException("경험 내용 다듬기 프롬프트를 찾을 수 없습니다.", AiErrorCode.E500_AI_GENERATION_FAILED)

        return aiChatClient.generateStructured(
            template.buildStructured(
                userPrompt = content,
                responseType = StarExperienceContents::class,
            ),
        )
    }

}
