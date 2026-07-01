package com.jobdori.core.domain.prompt

import com.jobdori.core.application.ai.command.AiGenerationRequest
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.application.ai.command.AiStructuredRequest
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import kotlin.reflect.KClass

data class PromptTemplate(
    val modelName: String,
    val parameters: AiParameters,
    val systemPrompt: String,   // DB에서 로드(튜닝 대상). USER 프롬프트는 DB에 두지 않는다(B안)
    val jsonSchema: String?
) {

    /** 자유 텍스트 생성 요청으로 빌드 (generateText용). userPrompt는 호출자가 만든다(JD/STAR=런타임 입력, 경험문장=코드 상수 템플릿 치환 결과) */
    fun build(userPrompt: String): AiGenerationRequest =
        AiGenerationRequest(
            modelName,
            systemPrompt,
            userPrompt,
            parameters
        )

    /** JSON 스키마 강제 구조화 요청으로 빌드 (generateStructured용) — 텍스트 경로와 대칭 */
    fun <T : Any> buildStructured(userPrompt: String, responseType: KClass<T>): AiStructuredRequest<T> =
        AiStructuredRequest(
            modelName,
            systemPrompt,
            userPrompt,
            parameters,
            responseType,
            jsonSchema ?: throw AiException("jsonSchema 누락", AiErrorCode.E500_AI_GENERATION_FAILED),
        )
}
