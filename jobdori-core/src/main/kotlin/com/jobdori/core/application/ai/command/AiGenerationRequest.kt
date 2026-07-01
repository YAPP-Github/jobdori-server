package com.jobdori.core.application.ai.command

data class AiGenerationRequest(
    val model: String,
    val systemPrompt: String,
    val userPrompt: String,
    val parameters: AiParameters,
)
