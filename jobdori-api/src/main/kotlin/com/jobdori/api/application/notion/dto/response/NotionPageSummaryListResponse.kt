package com.jobdori.api.application.notion.dto.response

import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.core.domain.notion.NotionPages

data class NotionPageSummaryListResponse(
    val pages: List<NotionPageSummaryResponse>,
    val cursor: CursorResponse,
) {

    companion object {
        fun from(slice: NotionPages) = NotionPageSummaryListResponse(
            pages = slice.pages.map { NotionPageSummaryResponse.from(it) },
            cursor = CursorResponse(nextCursor = slice.nextCursor),
        )
    }

}
