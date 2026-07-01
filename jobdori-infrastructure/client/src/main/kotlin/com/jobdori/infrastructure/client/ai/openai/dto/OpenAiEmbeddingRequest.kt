package com.jobdori.infrastructure.client.ai.openai.dto

data class OpenAiEmbeddingRequest(
    val model: String,
    val input: String
)
