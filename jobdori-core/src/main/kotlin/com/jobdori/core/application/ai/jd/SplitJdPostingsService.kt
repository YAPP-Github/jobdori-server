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
        val lines = body.lines()
        // 모델이 본문을 재출력하지 않도록 줄 번호를 붙여 보내고 줄 범위만 받는다(레이턴시는 출력 토큰에 비례)
        val numbered = lines.mapIndexed { i, line -> "${i + 1}| $line" }.joinToString("\n")
        val result = aiChatClient.generateStructured(template.buildStructured(numbered, JdPostingSplitResult::class))
        return result.postings
            .mapNotNull { slice(it, lines) }
            .distinctBy { it.title.ifBlank { it.body } }
            .take(JdPolicy.MAX_SPLIT_CANDIDATES)
            .ifEmpty { listOf(JdPosting(body = body)) }
    }

    /** 모델이 반환한 줄 범위가 어긋나도 등록이 죽지 않도록, 범위가 유효하지 않으면 버리고 폴백(전체 1건)에 맡긴다 */
    private fun slice(range: JdPostingSplitResult.PostingRange, lines: List<String>): JdPosting? {
        if (range.startLine < 1 || range.startLine > lines.size || range.endLine < range.startLine) return null
        val body = lines.subList(range.startLine - 1, minOf(range.endLine, lines.size))
            .joinToString("\n").trim()
        if (body.isBlank()) return null
        return JdPosting(title = range.title, body = body)
    }
}
