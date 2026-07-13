package com.jobdori.infrastructure.client.notion.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NotionSearchResponse(
    val results: List<NotionPageResponse> = emptyList(),

    @JsonProperty("next_cursor")
    val nextCursor: String? = null,

    @JsonProperty("has_more")
    val hasMore: Boolean = false,
)

data class NotionPageResponse(
    val id: String,
    val url: String? = null,

    @JsonProperty("last_edited_time")
    val lastEditedTime: String? = null,

    val properties: Map<String, NotionPropertyResponse> = emptyMap(),
)

data class NotionPropertyResponse(
    val type: String? = null,
    val title: List<NotionRichTextResponse>? = null,
)

data class NotionRichTextResponse(
    @JsonProperty("plain_text")
    val plainText: String? = null,
)
