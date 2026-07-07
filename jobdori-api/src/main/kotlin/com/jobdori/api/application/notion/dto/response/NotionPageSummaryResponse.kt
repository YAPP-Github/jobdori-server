package com.jobdori.api.application.notion.dto.response

import com.jobdori.core.domain.notion.NotionPageSummary
import java.time.LocalDateTime

data class NotionPageSummaryResponse(
    val id: String,
    val title: String,
    val url: String?,
    val lastEditedTime: LocalDateTime?,
) {

    companion object {
        fun from(page: NotionPageSummary) = NotionPageSummaryResponse(
            id = page.id,
            title = page.title,
            url = page.url,
            lastEditedTime = page.lastEditedTime,
        )
    }

}
