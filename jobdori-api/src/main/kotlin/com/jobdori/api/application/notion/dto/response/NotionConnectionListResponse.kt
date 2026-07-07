package com.jobdori.api.application.notion.dto.response

import com.jobdori.api.application.common.dto.response.CursorResponse
import com.jobdori.common.model.SliceResult
import com.jobdori.core.domain.notion.NotionConnection

data class NotionConnectionListResponse(
    val connections: List<NotionConnectionResponse>,
    val cursor: CursorResponse,
) {

    companion object {
        fun from(slice: SliceResult<NotionConnection>) = NotionConnectionListResponse(
            connections = slice.items.map { NotionConnectionResponse.from(it) },
            cursor = CursorResponse(nextCursor = slice.nextCursor),
        )
    }

}
