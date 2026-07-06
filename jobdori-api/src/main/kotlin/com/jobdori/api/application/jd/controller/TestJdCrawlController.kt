package com.jobdori.api.application.jd.controller

import com.jobdori.api.support.rest.ApiResponse
import com.jobdori.common.error.BaseException
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.application.jd.client.JdCrawlerClient
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * JD 크롤 + 메타 추출 검증용 테스트 엔드포인트(저장 없음 — 등록 기능은 별도).
 * URL을 크롤해 얻은 본문을 JD_META_EXTRACTION 프롬프트로 추출해 7필드를 그대로 돌려준다.
 * 크롤/추출 실패는 code와 함께 반환해 붙여넣기 유도 등 프론트 분기를 확인할 수 있게 한다.
 */
@Profile("local", "dev", "test")
@RestController
class TestJdCrawlController(
    private val jdCrawlerClient: JdCrawlerClient,
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {

    @PostMapping("/test-jd-crawl")
    fun run(@RequestParam url: String): ApiResponse<Any> {
        val result: Map<String, Any?> = try {
            val body = jdCrawlerClient.fetchBody(url)
            val template = promptTemplateRepository.findByType(PromptType.JD_META_EXTRACTION)
                ?: throw InvalidArgumentsException("프롬프트 없음: JD_META_EXTRACTION")
            mapOf(
                "url" to url,
                "crawledBody" to body,
                "extracted" to aiChatClient.generateStructured(template.buildStructured(body, Map::class)),
            )
        } catch (e: BaseException) {
            mapOf("url" to url, "error" to e.message, "code" to e.errorCode.code)
        }
        return ApiResponse.ok(result)
    }
}
