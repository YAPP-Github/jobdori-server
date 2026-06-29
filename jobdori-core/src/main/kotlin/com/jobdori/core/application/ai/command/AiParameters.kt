package com.jobdori.core.application.ai.command

data class AiParameters(
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val topP: Double? = null,
)
