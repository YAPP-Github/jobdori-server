package com.jobdori.core.application.ai.command

data class AiGenerationRequest(
    val model: String,
    val systemPrompt: String,
    val userPrompt: String,
    val parameters: AiParameters,
    /** 호출 목적 식별자(PromptType 이름). 사용량/비용 로그 집계 키로만 쓴다. */
    val useCase: String = "unknown",
)
