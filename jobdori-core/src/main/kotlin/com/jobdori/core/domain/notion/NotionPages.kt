package com.jobdori.core.domain.notion

data class NotionPages(
    val pages: List<NotionPageSummary>,
    val nextCursor: String?,
    val hasMore: Boolean,
)
