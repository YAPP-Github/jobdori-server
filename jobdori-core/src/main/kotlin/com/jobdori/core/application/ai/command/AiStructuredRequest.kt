package com.jobdori.core.application.ai.command

import kotlin.reflect.KClass

data class AiStructuredRequest<T: Any>(
    val model: String,
    val systemPrompt: String,
    val userPrompt: String,
    val parameters: AiParameters,
    val responseType: KClass<T>,
    val jsonSchema: String,
)
