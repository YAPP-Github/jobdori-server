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
        val postings = result.postings
            .mapNotNull { slice(it, lines) }
            .distinctBy { it.title.ifBlank { it.body } }
        // 후보가 원문 전체를 담고 있으면(줄바꿈 없는 본문 등) 그 후보로 재등록해도 다시 다중으로 판별돼 선택이 무한 반복된다
        if (postings.size > 1 && postings.any { it.body == body.trim() }) return listOf(JdPosting(body = body))
        return postings
            // 최소 길이 미달 조각은 재등록할 수 없고, 단건이어도 모델이 줄 범위를 잘못 준 것이므로 원문 폴백에 맡긴다
            .filter { it.body.length >= JdPolicy.MIN_JD_BODY_LENGTH }
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
