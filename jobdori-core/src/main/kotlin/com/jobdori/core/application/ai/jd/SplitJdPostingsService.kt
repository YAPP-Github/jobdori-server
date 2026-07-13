package com.jobdori.core.application.ai.jd

import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.ai.jd.result.JdPosting
import com.jobdori.core.application.ai.jd.result.JdPostingSplitResult
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.jd.JdPolicy
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.stereotype.Service

/** 동일 직무명 중복 제거 + 최대 [JdPolicy.MAX_SPLIT_CANDIDATES]건, 최소 1건을 보장한다(HM0002). */
@Service
class SplitJdPostingsService(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {
    fun split(body: String): List<JdPosting> {
        val template = promptTemplateRepository.findByType(PromptType.JD_MULTI_POSTING_SPLIT)
            ?: throw AiException("프롬프트 없음: JD_MULTI_POSTING_SPLIT", AiErrorCode.E500_AI_GENERATION_FAILED)
        val result = aiChatClient.generateStructured(template.buildStructured(body, JdPostingSplitResult::class))
        return result.postings
            .distinctBy { it.title.ifBlank { it.body } }
            .take(JdPolicy.MAX_SPLIT_CANDIDATES)
            .ifEmpty { listOf(JdPosting(body = body)) }
    }
}
