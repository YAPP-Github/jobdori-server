package com.jobdori.core.domain.notion

data class NotionPageContent(
    val page: NotionPageSummary,
    val plainText: String,
    val blocks: List<NotionBlock>,
)
