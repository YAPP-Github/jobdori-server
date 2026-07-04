package com.jobdori.api.application.prompt.controller

import com.jobdori.api.support.rest.ApiResponse
import com.jobdori.common.error.InvalidArgumentsException
import com.jobdori.common.pdf.PdfUtils
import com.jobdori.core.application.ai.client.AiChatClient
import com.jobdori.core.domain.ai.error.AiException
import com.jobdori.core.domain.prompt.PromptType
import com.jobdori.core.domain.prompt.repository.PromptTemplateRepository
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.multipart.MultipartFile

/**
 * 프롬프트 튜닝/검증용 테스트 엔드포인트.
 * promptType으로 DB의 SYSTEM 프롬프트+모델+파라미터+jsonSchema를 로드하고,
 * userPrompt(+첨부 PDF에서 추출한 텍스트)를 인풋으로 실제 AI 호출 결과를 그대로 돌려준다.
 *
 * multipart/form-data 로 호출한다(파일 첨부 가능). 예:
 *   promptType=JD_META_EXTRACTION
 *   userPrompt=(선택) 추가 지시/텍스트
 *   files=(선택) PDF 여러 개 — 텍스트 추출 후 userPrompt 뒤에 붙는다
 *   structured=(선택) true/false, 생략 시 jsonSchema 유무로 자동 판별
 */
@Profile("local", "dev", "test")
@RestController
class TestPromptController(
    private val promptTemplateRepository: PromptTemplateRepository,
    private val aiChatClient: AiChatClient,
) {

    @PostMapping("/test-prompts", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun run(
        @RequestParam promptType: PromptType,
        @RequestParam(required = false) userPrompt: String?,
        @RequestParam(required = false) structured: Boolean?,
        @RequestPart(required = false) files: List<MultipartFile>?,
    ): ApiResponse<Any> {
        val template = promptTemplateRepository.findByType(promptType)
            ?: throw InvalidArgumentsException("해당 타입의 프롬프트 템플릿이 없습니다: $promptType")

        // ponytail: 첨부는 PDF로 가정하고 텍스트만 추출해 인풋에 이어붙임. 다른 포맷 필요해지면 그때 분기.
        val fileTexts = files.orEmpty().filter { !it.isEmpty }.map { PdfUtils.extractText(it.bytes) }
        val finalUserPrompt = (listOfNotNull(userPrompt) + fileTexts)
            .joinToString("\n\n")
            .ifBlank { throw InvalidArgumentsException("userPrompt 또는 파일 중 하나는 있어야 합니다") }

        // 구조화 여부: 명시값 우선, 없으면 jsonSchema 유무로 판별
        val useStructured = structured ?: (template.jsonSchema != null)

        val output: Any = try {
            if (useStructured) {
                // ponytail: 테스트라 응답 타입을 특정할 수 없으니 Map으로 파싱해 구조화 JSON을 그대로 반환
                aiChatClient.generateStructured(template.buildStructured(finalUserPrompt, Map::class))
            } else {
                aiChatClient.generateText(template.build(finalUserPrompt))
            }
        } catch (e: AiException) {
            // 테스트 엔드포인트: 전역 마스킹 대신 OpenAI 원본 에러를 그대로 노출
            mapOf(
                "error" to e.message,
                "openAiResponse" to openAiRawBody(e),
            )
        }

        return ApiResponse.ok(
            mapOf(
                "promptType" to promptType,
                "model" to template.modelName,
                "structured" to useStructured,
                "systemPrompt" to template.systemPrompt,
                "userPrompt" to finalUserPrompt,
                "output" to output,
            ),
        )
    }

    /** AiException 원인 체인에서 OpenAI가 내려준 응답 본문을 추출(없으면 null) */
    private fun openAiRawBody(e: AiException): String? =
        generateSequence(e.cause) { it.cause }
            .filterIsInstance<HttpStatusCodeException>()
            .firstOrNull()
            ?.responseBodyAsString
}
