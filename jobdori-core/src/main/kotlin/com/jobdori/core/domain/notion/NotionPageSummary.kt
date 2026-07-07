package com.jobdori.core.domain.notion

import java.time.LocalDateTime

data class NotionPageSummary(
    val id: String,
    val title: String,
    val url: String?,
    val lastEditedTime: LocalDateTime?,
)
