package com.jobdori.infrastructure.client.notion.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class NotionBlockChildrenResponse(
    val results: List<NotionBlockResponse> = emptyList(),

    @JsonProperty("next_cursor")
    val nextCursor: String? = null,

    @JsonProperty("has_more")
    val hasMore: Boolean = false,
)

data class NotionBlockResponse(
    val id: String,
    val type: String,

    @JsonProperty("has_children")
    val hasChildren: Boolean = false,

    val paragraph: NotionRichTextBlockResponse? = null,

    @JsonProperty("heading_1")
    val heading1: NotionRichTextBlockResponse? = null,

    @JsonProperty("heading_2")
    val heading2: NotionRichTextBlockResponse? = null,

    @JsonProperty("heading_3")
    val heading3: NotionRichTextBlockResponse? = null,

    @JsonProperty("bulleted_list_item")
    val bulletedListItem: NotionRichTextBlockResponse? = null,

    @JsonProperty("numbered_list_item")
    val numberedListItem: NotionRichTextBlockResponse? = null,

    val quote: NotionRichTextBlockResponse? = null,
    val callout: NotionRichTextBlockResponse? = null,

    @JsonProperty("to_do")
    val toDo: NotionRichTextBlockResponse? = null,

    val toggle: NotionRichTextBlockResponse? = null,
    val code: NotionRichTextBlockResponse? = null,

    @JsonProperty("child_page")
    val childPage: NotionChildPageBlockResponse? = null,
)

data class NotionRichTextBlockResponse(
    @JsonProperty("rich_text")
    val richText: List<NotionRichTextResponse> = emptyList(),
)

data class NotionChildPageBlockResponse(
    val title: String? = null,
)
