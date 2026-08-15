package com.jobdori.infrastructure.client.ai.openai.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.jobdori.common.json.JsonUtils
import com.jobdori.core.application.ai.command.AiParameters
import com.jobdori.core.domain.ai.error.AiErrorCode
import com.jobdori.core.domain.ai.error.AiException
import tools.jackson.databind.JsonNode

/**
 * OpenAI Chat Completions API 요청 DTO (필요한 최소 필드만).
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
data class OpenAiChatCompletionRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double? = null,
    @JsonProperty("max_tokens") val maxTokens: Int? = null,
    @JsonProperty("top_p") val topP: Double? = null,
    @JsonProperty("response_format") val responseFormat: ResponseFormat? = null,
) {
    data class Message(val role: String, val content: Any) {
        fun textContent(): String = when (content) {
            is String -> content
            is List<*> -> content.filterIsInstance<ContentPart>().mapNotNull { it.text }.joinToString("\n")
            else -> ""
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class ContentPart(
        val type: String,
        val text: String? = null,
        @JsonProperty("image_url") val imageUrl: ImageUrl? = null,
    ) {
        data class ImageUrl(val url: String, val detail: String = "high")

        companion object {
            fun text(value: String) = ContentPart(type = "text", text = value)
            fun image(dataUrl: String) = ContentPart(type = "image_url", imageUrl = ImageUrl(dataUrl))
        }
    }

    data class ResponseFormat(
        val type: String,
        @JsonProperty("json_schema") val jsonSchema: JsonSchema,
    ) {
        companion object {
            fun jsonSchema(name: String, schemaJson: String) = ResponseFormat(
                type = "json_schema",
                jsonSchema = JsonSchema(
                    name = name,
                    schema = runCatching { JsonUtils.DEFAULT_JSON_MAPPER.readTree(schemaJson) }
                        .getOrElse { throw AiException("jsonSchema 파싱 실패", AiErrorCode.E500_AI_GENERATION_FAILED, it) },
                ),
            )
        }
    }

    data class JsonSchema(val name: String, val schema: JsonNode, val strict: Boolean = true)

    companion object {
        /** system은 있으면 추가, user는 항상 추가. 파라미터/포맷을 OpenAI 필드로 매핑. */
        fun of(
            model: String,
            system: String?,
            user: String,
            parameters: AiParameters,
            format: ResponseFormat? = null,
        ) = OpenAiChatCompletionRequest(
            model = model,
            messages = buildList {
                if (system != null) add(Message("system", system))
                add(Message("user", user))
            },
            temperature = parameters.temperature,
            maxTokens = parameters.maxTokens,
            topP = parameters.topP,
            responseFormat = format,
        )

        fun vision(
            model: String,
            system: String,
            userContent: List<ContentPart>,
            parameters: AiParameters,
        ) = OpenAiChatCompletionRequest(
            model = model,
            messages = listOf(
                Message("system", system),
                Message("user", userContent),
            ),
            temperature = parameters.temperature,
            maxTokens = parameters.maxTokens,
            topP = parameters.topP,
        )
    }
}
